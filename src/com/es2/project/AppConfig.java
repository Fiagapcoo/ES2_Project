package com.es2.project;

import java.io.IOException;
import java.util.Properties;

/**
 * Singleton class to store global application configurations.
 */
public class AppConfig {
    public static final String ALPHANUMERIC = "ALPHANUMERIC";
    public static final String SPECIAL = "SPECIAL";

    private static AppConfig instance;
    private String databaseUrl;
    private String encryptionKey;
    private int passwordLength;

    /**
     * Private constructor to initialize configurations.
     */
    private AppConfig() {
        Properties props = new Properties();
        String configFile = null;
        try {
            configFile = System.getProperty("config.file", "config.properties");
            props.load(getClass().getClassLoader().getResourceAsStream(configFile));
            this.databaseUrl = props.getProperty("database.url");
            this.encryptionKey = props.getProperty("encryption.key");
            this.passwordLength = Integer.parseInt(props.getProperty("password.length"));

            validate();
        } catch (IOException e) {
            throw new RuntimeException("Arquivo " + configFile + " não encontrado!", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato inválido para password.length", e);
        }
    }

    /**
     * Validation of configurations values.
     */
    private void validate() {
        if (passwordLength < 11) {
            throw new IllegalArgumentException("passwordLength deve ser de pelo menos 11");
        }
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalArgumentException("encryptionKey não pode estar vazia");
        }
    }

    /**
     * Returns the single instance of the configuration.
     *
     * @return Single instance of AppConfig.
     */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Gets the database URL.
     *
     * @return Database URL.
     */
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    /**
     * Gets the encryption key.
     *
     * @return Encryption key.
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }

    /**
     * Gets the default password length.
     *
     * @return Default password length.
     */
    public int getPasswordLength() {
        return passwordLength;
    }

    /**
     * Sets the database URL.
     *
     * @param newUrl The new database URL.
     */
    public synchronized void setDatabaseUrl(String newUrl) {
        this.databaseUrl = newUrl;
    }

    /**
     * Sets the encryption key.
     *
     * @param newKey The new encryption key.
     * @throws IllegalArgumentException if the key is empty or null.
     */
    public synchronized void setEncryptionKey(String newKey) {
        if (newKey == null || newKey.isBlank()) {
            throw new IllegalArgumentException("EncryptionKey não pode ser vazia!");
        }
        this.encryptionKey = newKey;
    }

    /**
     * Sets the password length.
     *
     * @param newLength The new password length.
     * @throws IllegalArgumentException if the length is less than 11.
     */
    public synchronized void setPasswordLength(int newLength) {
        if (newLength < 11) {
            throw new IllegalArgumentException("Tamanho mínimo: 11!");
        }
        this.passwordLength = newLength;
    }

    /**
     * Generates a password using the specified type.
     *
     * @param type The type of password generator ("ALPHANUMERIC" or "SPECIAL").
     * @return A generated password.
     */
    public String generatePassword(String type) {
        PasswordGenerator generator = PasswordGeneratorFactory.createGenerator(type);
        return generator.generate(passwordLength);
    }

}
