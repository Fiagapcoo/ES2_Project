package com.es2.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageManager {

    private static StorageManager instance;
    private PasswordStorage passwordStorage;
    private CryptoManager cryptoManager;
    private final AppStateManager appStateManager;

    public StorageManager(PasswordStorage passwordStorage) {
        this.passwordStorage = passwordStorage;
        this.cryptoManager = CryptoManager.getInstance();
        this.appStateManager = AppStateManager.getInstance();
    }

    private CryptoManager getCryptoManager() {
        cryptoManager =  CryptoManager.getInstance();
        return cryptoManager;
    }

    public static synchronized StorageManager getInstance() {
        if (instance == null) {
            PasswordStorage defaultStorage = new FilePasswordStorage(AppConfig.getInstance().get_path());
            instance = new StorageManager(defaultStorage);
        }
        return instance;
    }

    public static synchronized void reloadInstance() {
        PasswordStorage defaultStorage = new FilePasswordStorage(AppConfig.getInstance().get_path());
        instance = new StorageManager(defaultStorage);
    }


    public synchronized void setPasswordStorage(PasswordStorage passwordStorage) {
        this.passwordStorage = passwordStorage;
    }

    public void savePassword(String categoryName, String password) {
        String encryptedPassword = getCryptoManager().encrypt(password);
        passwordStorage.savePassword(categoryName, encryptedPassword);
        appStateManager.recordAccess(categoryName, encryptedPassword, "modification");
    }

    public void saveEncryptedPassword(String categoryName, String encryptedPassword) {
        passwordStorage.savePassword(categoryName, encryptedPassword);
    }


    public String loadPassword(String categoryName) {
        String encryptedPassword = passwordStorage.loadPassword(categoryName);
        if (encryptedPassword == null) {
            return null;
        }
        String decryptedPassword = getCryptoManager().decrypt(encryptedPassword);
        appStateManager.recordAccess(categoryName, decryptedPassword, "consultation");
        return decryptedPassword;
    }

    public void restorePasswordsFromState(Map<String, List<AppStateManager.AccessInfo>> stateData) {
        for (Map.Entry<String, List<AppStateManager.AccessInfo>> entry : stateData.entrySet()) {
            String category = entry.getKey();
            List<AppStateManager.AccessInfo> history = entry.getValue();

            if (history != null && !history.isEmpty()) {
                for (int i = history.size() - 1; i >= 0; i--) {
                    AppStateManager.AccessInfo access = history.get(i);
                    if ("modification".equalsIgnoreCase(access.getAction())) {
                        // Salve a senha já criptografada diretamente
                        saveEncryptedPassword(category, access.getPassword());
                        break;
                    }
                }
            }
        }
    }
    }