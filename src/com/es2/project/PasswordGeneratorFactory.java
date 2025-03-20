package com.es2.project;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to create different types of password generators.
 */
public class PasswordGeneratorFactory {
    private static final Map<String, PasswordGenerator> generators = new HashMap<>();

    static {
        register("ALPHANUMERIC", new AlphanumericPasswordGenerator());
        register("SPECIAL", new SpecialCharPasswordGenerator());
    }

    public static void register(String type, PasswordGenerator generator) {
        if (type == null || generator == null) {
            throw new IllegalArgumentException("Type/generator cannot be null!");
        }
        generators.put(type.toUpperCase(), generator);
    }

    public static PasswordGenerator createGenerator(String type) {
        PasswordGenerator generator = generators.get(type.toUpperCase());
        if (generator == null) {
            throw new IllegalArgumentException("Invalid type: " + type + ". Valid types: " + generators.keySet());
        }
        return generator;
    }
}
