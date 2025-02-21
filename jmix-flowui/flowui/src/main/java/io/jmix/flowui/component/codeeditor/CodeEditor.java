/*
 * Copyright 2023 Haulmont.
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
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorTheme;
import io.jmix.flowui.service.SpringContextAwareGroovyScriptEvaluator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.scripting.ScriptEvaluator;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CodeEditor extends VerticalLayout implements SupportsValueSource<String>, SupportsValidation<String>, SupportsStatusChangeHandler<AceGroovyCodeEditor>,
        HasRequired, ApplicationContextAware, InitializingBean, HasHelper, HasLabel,
        Focusable, HasTitle, HasTooltip, HasValueAndElement<AbstractField.ComponentValueChangeEvent<AceEditor, String>, String> {
    public static final CodeEditorMode MODE_SQL = CodeEditorMode.SQL;
    public static final CodeEditorMode MODE_GROOVY = CodeEditorMode.GROOVY;
    public static final CodeEditorMode MODE_TEXT = CodeEditorMode.TEXT;

    private final VerticalLayout tabSheet = new VerticalLayout();
    private final NativeLabel nativeLabel = new NativeLabel();
    VerticalLayout helperLayout = new VerticalLayout();
    private AceGroovyCodeEditor aceGroovyCodeEditor;
    private Div helperDiv = new Div();
    private ApplicationContext applicationContext;

    public CodeEditor() {
        tabSheet.setSizeFull();
        add(tabSheet);
        helperDiv.setWidthFull();
        nativeLabel.getStyle().set("color", "red");
    }

    private com.vaadin.flow.component.Component createEditorComponent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(helperDiv);
        layout.add(aceGroovyCodeEditor);
        layout.add(nativeLabel);
        return layout;
    }


    public void setCustomAutoCompletion(Collection<String> suggestions) {
        aceGroovyCodeEditor.setCustomAutoCompletion(suggestions.stream().collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        initComponent();
    }

    private void initComponent() {
        this.aceGroovyCodeEditor = new AceGroovyCodeEditor();
        aceGroovyCodeEditor.setApplicationContext(applicationContext);
        tabSheet.add(createEditorComponent());

        aceGroovyCodeEditor.addValueChangeListener(e -> {
            if (applicationContext.getBean(ScriptEvaluator.class) instanceof SpringContextAwareGroovyScriptEvaluator se) {
                nativeLabel.setText(se.checkSyntax(e.getValue()));
            }
        });
    }

    @Override
    public void afterPropertiesSet() {
        aceGroovyCodeEditor.afterPropertiesSet();
    }

    @Override
    public Registration addValidator(Validator<? super String> validator) {
        return aceGroovyCodeEditor.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        aceGroovyCodeEditor.executeValidators();
    }

    protected void validate() {
        aceGroovyCodeEditor.validate();
    }

    @Override
    public boolean isInvalid() {
        return aceGroovyCodeEditor.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        aceGroovyCodeEditor.setInvalid(invalid);
    }

    @Nullable
    @Override
    public String getErrorMessage() {
        return aceGroovyCodeEditor.getErrorMessage();
    }

    @Override
    public void setErrorMessage(@Nullable String errorMessage) {
        aceGroovyCodeEditor.setErrorMessage(errorMessage);
    }

    @Override
    public void setStatusChangeHandler(@Nullable Consumer<StatusContext<AceGroovyCodeEditor>> handler) {
        aceGroovyCodeEditor.setStatusChangeHandler(handler);
    }

    @Nullable
    @Override
    public ValueSource<String> getValueSource() {
        return aceGroovyCodeEditor.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<String> valueSource) {
        aceGroovyCodeEditor.setValueSource(valueSource);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return aceGroovyCodeEditor.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        aceGroovyCodeEditor.setRequiredMessage(requiredMessage);
    }

    public void setRequired(boolean required) {
        aceGroovyCodeEditor.setRequiredIndicatorVisible(required);
    }

    public void setRequiredIndicatorVisible(boolean required) {
        aceGroovyCodeEditor.setRequiredIndicatorVisible(required);
    }

    public String getValue() {
        return aceGroovyCodeEditor.getValue();
    }

    public void setValue(String value) {
        aceGroovyCodeEditor.setValue(Strings.nullToEmpty(value));
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<AceEditor, String>> valueChangeListener) {
        return aceGroovyCodeEditor.addValueChangeListener(valueChangeListener);
    }

    @Override
    public void setHelperComponent(Component html) {
        this.helperDiv.removeAll();
        helperDiv.add(html);
    }

    public CodeEditorMode getMode() {
        var mode = aceGroovyCodeEditor.getMode().name();
        if (mode != null) {
            return CodeEditorMode.fromId(mode);
        }
        return null;
    }

    public void setMode(CodeEditorMode codeEditorMode) {
        Arrays.stream(AceMode.values()).filter(v -> v.name().equals(codeEditorMode.getId())).findFirst().ifPresent(v -> aceGroovyCodeEditor.setMode(v));
    }

    public void setTheme(CodeEditorTheme aceTheme) {
        if (aceTheme != null) {
            Arrays.stream(AceTheme.values()).filter(act -> act.name().equals(aceTheme.getId())).findFirst().ifPresent(v -> aceGroovyCodeEditor.setTheme(v));
        }
    }

    public void setUseSoftTabs(Boolean aBoolean) {
        aceGroovyCodeEditor.setUseSoftTabs(aBoolean);
    }

    public void setTextWrap(Boolean aBoolean) {
        aceGroovyCodeEditor.setTextWrap(aBoolean);
    }

    public void setFontSize(String s) {
        aceGroovyCodeEditor.setFontSize(s);
    }

    public void setPrintMarginColumn(Integer integer) {
        aceGroovyCodeEditor.setPrintMarginColumn(integer);
    }

    public void setShowPrintMargin(Boolean aBoolean) {
        aceGroovyCodeEditor.setShowPrintMargin(aBoolean);
    }

    public void setShowLineNumbers(Boolean aBoolean) {
        aceGroovyCodeEditor.setShowLineNumbers(aBoolean);
    }

    public void setShowGutter(Boolean aBoolean) {
        aceGroovyCodeEditor.setShowGutter(aBoolean);
    }

    public void setHighlightGutterLine(Boolean aBoolean) {
        aceGroovyCodeEditor.setHighlightGutterLine(aBoolean);
    }

    public void setHighlightActiveLine(Boolean aBoolean) {
        aceGroovyCodeEditor.setHighlightActiveLine(aBoolean);
    }

}
