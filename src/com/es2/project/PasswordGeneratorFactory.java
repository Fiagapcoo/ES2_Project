package com.es2.project;

/**
 * Factory class to create different types of password generators.
 */
public class PasswordGeneratorFactory {
    /**
     * Returns an instance of a password generator based on the type.
     *
     * @param type The type of password generator ("ALPHANUMERIC" or "SPECIAL").
     * @return An instance of PasswordGenerator.
     */
    public static PasswordGenerator createGenerator(String type) {
        return switch (type.toUpperCase()) {
            case "SPECIAL" -> new SpecialCharPasswordGenerator();
            case "ALPHANUMERIC" -> new AlphanumericPasswordGenerator();
            default -> throw new IllegalArgumentException("Invalid password generator type: " + type);
        };
    }
}
