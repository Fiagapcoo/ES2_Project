package com.es2.project;
/**
 * Singleton class to store global application configurations.
 */
public class AppConfig {
    private static AppConfig instance;
    private String databaseUrl;
    private String encryptionKey;
    private int passwordLength;

    /**
     * Private constructor to initialize configurations.
     *
     * @param databaseUrl   Database URL.
     * @param encryptionKey Encryption key.
     * @param passwordLength Default password length.
     */
    private AppConfig(String databaseUrl, String encryptionKey, int passwordLength) {
        this.databaseUrl = databaseUrl;
        this.encryptionKey = encryptionKey;
        this.passwordLength = passwordLength;
    }

    /**
     * Returns the single instance of the configuration.
     *
     * @param databaseUrl   Database URL.
     * @param encryptionKey Encryption key.
     * @param passwordLength Default password length.
     * @return Single instance of AppConfig.
     */
    public static synchronized AppConfig getInstance(String databaseUrl, String encryptionKey, int passwordLength) {
        if (instance == null) {
            instance = new AppConfig(databaseUrl, encryptionKey, passwordLength);
        }
        return instance;
    }

    /**
     * Gets the database URL.
     *
     * @return Database URL.
     */
    public String getDatabaseUrl() {
        return this.databaseUrl;
    }

    /**
     * Sets the database URL.
     *
     * @param databaseUrl New database URL.
     */
    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    /**
     * Gets the encryption key.
     *
     * @return Encryption key.
     */
    public String getEncryptionKey() {
        return this.encryptionKey;
    }

    /**
     * Sets the encryption key.
     *
     * @param encryptionKey New encryption key.
     */
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * Gets the default password length.
     *
     * @return Default password length.
     */
    public int getPasswordLength() {
        return this.passwordLength;
    }

    /**
     * Sets the default password length.
     *
     * @param passwordLength New default password length.
     */
    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
    }
}