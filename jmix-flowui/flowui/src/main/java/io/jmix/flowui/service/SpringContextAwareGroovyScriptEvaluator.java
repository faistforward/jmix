package io.jmix.flowui.service;


import com.vaadin.flow.spring.annotation.UIScope;
import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import io.jmix.core.annotation.GroovyContextBean;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpringContextAwareGroovyScriptEvaluator extends GroovyScriptEvaluator {
    private final ClassLoader classLoader;
    private final ApplicationContext applicationContext;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Object> springBeans = new HashMap<>();

    public SpringContextAwareGroovyScriptEvaluator(ClassLoader classLoader, ApplicationContext applicationContext) {
        super(classLoader);
        this.classLoader = classLoader;
        this.applicationContext = applicationContext;
        createSpringBindings();
    }

    @Override
    @Nullable
    public Object evaluate(ScriptSource script, @Nullable Map<String, Object> arguments) {
        putUiScopedBean();
        Map args = new HashMap(arguments);
        args.putAll(springBeans);
        Binding binding = new Binding(args);

        GroovyShell groovyShell = new GroovyShell(this.classLoader, binding, getCompilerConfiguration());
        try {
            String filename = (script instanceof ResourceScriptSource resourceScriptSource ?
                    resourceScriptSource.getResource().getFilename() : null);
            if (filename != null) {
                return groovyShell.evaluate(script.getScriptAsString(), filename);
            } else {
                return groovyShell.evaluate(script.getScriptAsString());
            }
        } catch (IOException ex) {
            throw new ScriptCompilationException(script, "Cannot access Groovy script", ex);
        } catch (GroovyRuntimeException ex) {
            throw new ScriptCompilationException(script, ex);
        }
    }

    public String checkSyntax(String script) {
        GroovyShell shell = new GroovyShell();
        try {
            // Versuche, den Code zu parsen. Dies führt zu einer Syntaxprüfung.
            shell.parse(script);
            return null; // Syntax ist in Ordnung
        } catch (CompilationFailedException e) {
            // Es wurde ein Syntaxfehler gefunden
            return "Syntax-Fehler: " + e.getMessage();
        } catch (Exception e) {
            // Andere Fehlerfälle abfangen
            return "Fehler: " + e.getMessage();
        }
    }

    protected void createSpringBindings() {
        // Zuerst alle Singleton-Beans erfassen
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (!springBeans.containsKey(beanName)) {
                try {
                    if (applicationContext.isSingleton(beanName)) {
                        Object bean = applicationContext.getBean(beanName);
                        if (bean.getClass().isAnnotationPresent(GroovyContextBean.class)) {
                            springBeans.put(beanName, bean);
                        }
                    }
                } catch (Exception ex) {
                    log.trace("Failed to evaluate bean [" + beanName + "]: " + ex.getMessage(), ex);
                }
            }
        }
    }

    private void putUiScopedBean() {
        Map<String, Object> uiScopedBeans = applicationContext.getBeansWithAnnotation(UIScope.class);
        uiScopedBeans.entrySet().stream().filter(e -> e.getValue().getClass().isAnnotationPresent(GroovyContextBean.class)).forEach(e -> {
            springBeans.put(e.getKey(), e.getValue());
        });
    }

    public Map<String, Object> getAllBeansOfScope() {
        putUiScopedBean();
        Map<String, Object> allBeans = new HashMap<>();
        allBeans.putAll(springBeans);
        return allBeans;
    }

}
