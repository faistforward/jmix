package io.jmix.dynattrflowui.utils;

import io.jmix.dynattrflowui.impl.model.AttributeLocalizedEnumValue;

import java.util.HashMap;
import java.util.Map;

public class DynEnumStringParser {
    /**
     * Wandelt einen String im Format "[key;value],[key2;value2]..." in eine Map um.
     * Diese Implementierung durchsucht den String nur einmal und vermeidet
     * unnötige Zwischenspeicherungen und Array-Erstellungen.
     *
     * @param input der Eingabestring
     * @return eine Map mit den extrahierten Key-Value-Paaren
     */
    public static Map<String, String> parseStringToMap(String input) {
        Map<String, String> result = new HashMap<>();

        if (input == null || input.isEmpty()) {
            return result;
        }

        int length = input.length();
        int index = 0;

        while (index < length) {
            // Finde das nächste '['
            int startBracket = input.indexOf('[', index);
            if (startBracket == -1) {
                break;
            }
            // Finde das Semikolon als Trenner zwischen Key und Value
            int semicolon = input.indexOf(';', startBracket);
            if (semicolon == -1) {
                break;
            }
            // Finde das schließende ']' des Paares
            int endBracket = input.indexOf(']', semicolon);
            if (endBracket == -1) {
                break;
            }

            // Extrahiere Key und Value und trimme eventuelle Leerzeichen
            String key = input.substring(startBracket + 1, semicolon).trim();
            String value = input.substring(semicolon + 1, endBracket).trim();
            result.put(key, value);

            // Setze den Index nach dem aktuellen ']'
            index = endBracket + 1;
        }

        return result;
    }

    public static String toEnumString(AttributeLocalizedEnumValue attr) {
        if (attr == null) {
            return null;
        }
        return toEnumString(attr.getValue(), attr.getExtValue());
    }

    public static String toEnumString(String key, String value) {
        return "[" + key + ";" + value + "]";
    }
}
