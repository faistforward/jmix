/*
 * Copyright 2025 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.component.codeeditor;

import com.google.common.base.Strings;
import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.service.SpringContextAwareGroovyScriptEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.scripting.ScriptEvaluator;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AceGroovyCodeEditor extends AceEditor implements SupportsValueSource<String>, SupportsValidation<String>, SupportsStatusChangeHandler<AceGroovyCodeEditor>,
        HasRequired, ApplicationContextAware, InitializingBean {

    protected final Set<String> currentScriptVaraibles = new HashSet<>();
    protected final Map<String, Set<String>> categorySuggestions = new HashMap<>();

    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected ApplicationContext applicationContext;
    protected FieldDelegate<AceGroovyCodeEditor, String, String> fieldDelegate;
    private ScheduledFuture<?> debounceFuture;

    public AceGroovyCodeEditor() {
        setTheme(AceTheme.github);
        setMode(AceMode.groovy);
        setFontSize(15);
        setHeight("100%");
        setWidth("100%");

        setReadOnly(false);
        setShowInvisibles(false);
        setShowGutter(true);
        setShowPrintMargin(true);
        setDisplayIndentGuides(true);
        setUseWorker(false);

        setSofttabs(true);
        setTabSize(3);
        setWrap(false);
        setMinlines(10);
        setMaxlines(50);
        setPlaceholder("");
        setAutoComplete(true);
        setLiveAutocompletion(true);
        setHighlightActiveLine(true);
        setHighlightSelectedWord(true);
        setMinHeight("200px");


        getElement().addEventListener("input", event -> {
            if (debounceFuture != null && !debounceFuture.isDone()) {
                debounceFuture.cancel(false);
            }
            final Element editorElement = getElement();
            final UI ui = UI.getCurrent();
            debounceFuture = scheduler.schedule(() -> {
                ui.access(() -> {
                    editorElement
                            .executeJs("return this.editor.getValue();")
                            .then(String.class, value -> {
                                parseGroovyVariables(value);
                                upadteSuggestions();
                            });
                });
            }, 2, TimeUnit.SECONDS);
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        fieldDelegate = createFieldDelegate();
    }

    @Override
    public Registration addValidator(Validator<? super String> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    protected void validate() {
        fieldDelegate.updateInvalidState();
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        // Method is called from constructor so delegate can be null
        if (fieldDelegate != null) {
            fieldDelegate.setInvalid(invalid);
        } else {
            getElement().setProperty("invalid", invalid);
        }
    }

    @Nullable
    @Override
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    @Override
    public void setErrorMessage(@Nullable String errorMessage) {
        fieldDelegate.setErrorMessage(errorMessage);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<AceGroovyCodeEditor>> handler) {
        fieldDelegate.setStatusChangeHandler(handler);
    }

    @Nullable
    @Override
    public ValueSource<String> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<String> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);
    }

    @Override
    public void setValue(String value) {
        super.setValue(Strings.nullToEmpty(value));
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequiredIndicatorVisible(required);
        fieldDelegate.updateRequiredState();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
        fieldDelegate.updateRequiredState();
    }

    @SuppressWarnings("unchecked")
    protected FieldDelegate<AceGroovyCodeEditor, String, String> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    private void upadteSuggestions() {
        // Prepare if Editor can have multiple Categories at the same time
        categorySuggestions.put("Default", DefaultGroovySuggestions.suggestions);
        categorySuggestions.put("String", DefaultGroovySuggestions.getStringSuggestions());
        categorySuggestions.put("Integer", DefaultGroovySuggestions.getIntegerSuggestions());
        categorySuggestions.put("Double", DefaultGroovySuggestions.getDoubleSuggestions());
        categorySuggestions.put("LocalDate", DefaultGroovySuggestions.getLocalDateSuggestions());
        categorySuggestions.put("LocalDateTime", DefaultGroovySuggestions.getLocalDateTimeSuggestions());
        categorySuggestions.put("Variables", currentScriptVaraibles);

        if (applicationContext.getBean("scriptEvaluator", ScriptEvaluator.class) instanceof SpringContextAwareGroovyScriptEvaluator ev) {
            categorySuggestions.put("spring", DefaultGroovySuggestions.getSpringClassSuggestions(ev.getAllBeansOfScope()));
        }
        setCustomAutoCompletionCategory(categorySuggestions);
    }


    public void setCustomAutoCompletion(Set<String> suggestions) {
        categorySuggestions.put("Custom", suggestions);
        upadteSuggestions();
    }

    protected void setCustomAutoCompletionCategory(Map<String, Set<String>> suggestions) {
        String[] suggestionsArray = suggestions.values().stream().flatMap(Collection::stream).toList().toArray(String[]::new);
        setCustomAutoCompletion(suggestionsArray);
    }

    public void parseGroovyVariables(String groovyScript) {
        currentScriptVaraibles.clear();
        String variableRegex = "\\b(def|int|String|boolean|float|double|long|char)\\s+([a-zA-Z_$][a-zA-Z\\d_$]*)";
        Pattern pattern = Pattern.compile(variableRegex);
        Matcher matcher = pattern.matcher(groovyScript);

        while (matcher.find()) {
            currentScriptVaraibles.add(matcher.group(2));
        }
    }


    public void setTextWrap(Boolean aBoolean) {
        setWrap(aBoolean);
    }

    public void setUseSoftTabs(Boolean aBoolean) {
        setSofttabs(aBoolean);
    }

    public void setFontSize(String s) {
        if (StringUtils.isNotBlank(s)) {
            setFontSize(Integer.parseInt(s.replaceAll("\\D+", "")));
        }
    }

    public void setPrintMarginColumn(Integer integer) {
    }

    public void setShowLineNumbers(Boolean aBoolean) {

    }

    public void setHighlightGutterLine(Boolean aBoolean) {
    }

}
