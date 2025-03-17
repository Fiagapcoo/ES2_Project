package com.es2.project;

/**
 * Manager class responsible for updating application configurations.
 */
public class AppConfigManager {

    /**
     * Updates the database URL.
     *
     * @param newDatabaseUrl The new database URL.
     */
    public static void setDatabaseUrl(String newDatabaseUrl) {
        AppConfig instance = AppConfig.getInstance();
        updateField(instance, "databaseUrl", newDatabaseUrl);
    }

    /**
     * Updates the encryption key.
     *
     * @param newEncryptionKey The new encryption key.
     */
    public static void setEncryptionKey(String newEncryptionKey) {
        AppConfig instance = AppConfig.getInstance();
        updateField(instance, "encryptionKey", newEncryptionKey);
    }

    /**
     * Updates the password length.
     *
     * @param newPasswordLength The new password length.
     */
    public static void setPasswordLength(int newPasswordLength) {
        if (newPasswordLength < 11) {
            throw new IllegalArgumentException("Password length must be at least 11");
        }
        AppConfig instance = AppConfig.getInstance();
        updateField(instance, "passwordLength", newPasswordLength);
    }

    /**
     * Uses reflection to update a private field in the AppConfig singleton.
     *
     * @param instance The singleton instance.
     * @param fieldName The name of the field to update.
     * @param newValue The new value for the field.
     */
    private static void updateField(AppConfig instance, String fieldName, Object newValue) {
        try {
            var field = AppConfig.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, newValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update field: " + fieldName, e);
        }
    }
}
