package com.es2.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageManager {

    private PasswordStorage passwordStorage;
    private final CryptoManager cryptoManager;
    private final AppStateManager appStateManager;

    public StorageManager(PasswordStorage passwordStorage) {
        this.passwordStorage = passwordStorage;
        this.cryptoManager = CryptoManager.getInstance();
        this.appStateManager = AppStateManager.getInstance();
    }

    public void setPasswordStorage(PasswordStorage passwordStorage) {
        this.passwordStorage = passwordStorage;
    }

    public void savePassword(String categoryName, String password) {
        String encryptedPassword = cryptoManager.encrypt(password);
        passwordStorage.savePassword(categoryName, encryptedPassword);
        appStateManager.recordAccess(categoryName, password, "modification");
    }


    public String loadPassword(String categoryName) {
        String encryptedPassword = passwordStorage.loadPassword(categoryName);
        if (encryptedPassword == null) {
            return null;
        }
        String decryptedPassword = cryptoManager.decrypt(encryptedPassword);
        appStateManager.recordAccess(categoryName, decryptedPassword, "consultation");
        return decryptedPassword;
    }

    public void restorePasswordsFromState(Map<String, List<AppStateManager.AccessInfo>> stateData) {
        for (Map.Entry<String, List<AppStateManager.AccessInfo>> entry : stateData.entrySet()) {
            String category = entry.getKey();
            List<AppStateManager.AccessInfo> history = entry.getValue();

            if (history != null && !history.isEmpty()) {
                // Procurar o último AccessInfo com ação "modification"
                for (int i = history.size() - 1; i >= 0; i--) {
                    AppStateManager.AccessInfo access = history.get(i);
                    if ("modification".equalsIgnoreCase(access.getAction())) {
                        savePassword(category, access.getPassword());
                        break;
                    }
                }
            }
        }
    }
    }