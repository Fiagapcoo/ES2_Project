package com.es2.project;

import java.security.SecureRandom;

/**
 * Generates passwords containing only alphanumeric characters (A-Z, a-z, 0-9).
 */
public class AlphanumericPasswordGenerator implements PasswordGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    //private static final String CHARACTERS = "!!!!@@@@@@@@@@@@@@@££££££§§€€§€§€£§£§£§€£§";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}
