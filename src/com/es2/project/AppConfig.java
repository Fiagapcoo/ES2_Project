package com.es2.project;

import java.io.IOException;
import java.util.Properties;

/**
 * Singleton class to store global application configurations.
 */
public class AppConfig {
    private static AppConfig instance;
    private final String databaseUrl;
    private final String encryptionKey;
    private final int passwordLength;

    /**
     * Private constructor to initialize configurations.
     */
    private AppConfig() {
        Properties props = new Properties();
        try {
            // Carrega o arquivo do classpath (src/main/resources/config.properties)
            props.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            this.databaseUrl = props.getProperty("database.url");
            this.encryptionKey = props.getProperty("encryption.key");
            this.passwordLength = Integer.parseInt(props.getProperty("password.length"));

            validate();
        } catch (IOException e) {
            throw new RuntimeException("Arquivo config.properties não encontrado!", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato inválido para password.length", e);
        }
    }


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
     * @return Single instance of AppConfig.
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
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
}