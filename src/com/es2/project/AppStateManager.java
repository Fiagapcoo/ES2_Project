package com.es2.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppStateManager {
    public static class AccessInfo {
        String password;
        String action;

        AccessInfo(String password, String action) {
            this.password = password;
            this.action = action;
        }

        public String getPassword() {
            return password;
        }

        public String getAction() {
            return action;
        }
    }

    private static AppStateManager instance;
    private Map<String, List<AccessInfo>> accessedPasswords = new HashMap<>();

    public static synchronized AppStateManager getInstance() {
        if (instance == null) {
            instance = new AppStateManager();
        }
        return instance;
    }

    public void recordAccess(String categoryName, String password, String action) {
        AccessInfo accessInfo = new AccessInfo(password, action);
        accessedPasswords.computeIfAbsent(categoryName, k -> new ArrayList<>())
                .add(new AccessInfo(password, action));

    }

    public List<AccessInfo> getAccessHistory(String categoryName) {
        return accessedPasswords.getOrDefault(categoryName, new ArrayList<>());
    }

    public String getLastAccessedPassword(String categoryName) {
        List<AccessInfo> history = accessedPasswords.get(categoryName);
        if (history == null || history.isEmpty()) return null;
        return history.get(history.size() - 1).password;
    }

    public String getLastAccessedAction(String categoryName) {
        List<AccessInfo> history = accessedPasswords.get(categoryName);
        if (history == null || history.isEmpty()) return null;
        return history.get(history.size() - 1).action;
    }


    public AppState saveState() {
        AppConfig config = AppConfig.getInstance();
        return new AppState(accessedPasswords, config.getDatabaseUrl(), config.getEncryptionKey(), config.getPasswordLength());
    }

    public void restore(AppState state) {
        this.accessedPasswords = state.getState();

        AppConfig config = AppConfig.getInstance();
        config.setDatabaseUrl(state.getDatabaseUrl());
        config.setEncryptionKey(state.getEncryptionKey());
        config.setPasswordLength(state.getPasswordLength());

        CryptoManager.reload(state.getEncryptionKey());

        StorageManager.getInstance().restorePasswordsFromState(this.accessedPasswords);
    }

    public Map<String, List<AccessInfo>> getAccessedInfoMap() {
        return accessedPasswords;
    }

    public void displayList(String categoryName) {
        List<AccessInfo> turmaHistory = getAccessHistory(categoryName);

        for (AppStateManager.AccessInfo info : turmaHistory) {
            System.out.println("Password: " + info.getPassword() + " | Ação: " + info.getAction());
        }
    }
}
