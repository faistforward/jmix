package io.jmix.flowui.component.codeeditor;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultGroovySuggestions {
    public static final Set<String> suggestions =
            Set.of("value",
                    "entity",
                    "import io.jmix.core.entity.EntityValues;",
                    "import groovy.json.JsonSlurper;",
                    "import groovy.json.JsonOutput;",
                    "import groovy.time.TimeCategory;",
                    "import groovy.transform.*;",
                    "import java.time.*;",
                    "EntityValues.getValue(entity,arg)");


    public static Set<String> getStringSuggestions() {
        return gibAllePublicMethodenAlsSet(String.class, "");
    }

    public static Set<String> getIntegerSuggestions() {
        return gibAllePublicMethodenAlsSet(Integer.class, "");
    }

    public static Set<String> getDoubleSuggestions() {
        return gibAllePublicMethodenAlsSet(Double.class, "");
    }

    public static Set<String> getLocalDateSuggestions() {
        return gibAllePublicMethodenAlsSet(LocalDate.class, "");
    }

    public static Set<String> getLocalDateTimeSuggestions() {
        return gibAllePublicMethodenAlsSet(LocalDateTime.class, "");
    }


    public static Set<String> getSpringClassSuggestions(Map<String, Object> clazzMap) {
        return clazzMap.entrySet().stream().flatMap(e -> gibAllePublicMethodenAlsSet(e.getValue().getClass(), e.getKey() + ".").stream()).collect(Collectors.toUnmodifiableSet());
    }

    public static Set<String> gibAllePublicMethodenAlsSet(Class clazz, String prefix) {
        return Arrays.stream(clazz.getMethods())
                .map(method -> {
                    if (method.getParameterCount() > 0) {
                        return prefix + method.getName() + "(" + getParameters(method) + ");";
                    } else {
                        return prefix + method.getName() + "();";
                    }
                })
                .collect(Collectors.toSet());
    }

    private static String getParameters(Method method) {
        return Arrays.stream(method.getParameters()).map(p -> "arg_" + p.getType().getSimpleName()).collect(Collectors.joining(" "));
    }
}
