package com.es2.project;

/**
 * Interface for password generation strategies.
 */
public interface PasswordGenerator {
    /**
     * Generates a password with a specified length.
     *
     * @param length The length of the password.
     * @return A generated password.
     */
    String generate(int length);
}