package com.es2.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppState {
    private final Map<String, List<AppStateManager.AccessInfo>> state;
    private final String databaseUrl;
    private final String encryptionKey;
    private final int passwordLength;

    public AppState(Map<String, List<AppStateManager.AccessInfo>> state, String databaseUrl, String encryptionKey, int passwordLength) {
        this.state = new HashMap<>();
        for (Map.Entry<String, List<AppStateManager.AccessInfo>> entry : state.entrySet()) {
            this.state.put(entry.getKey(), new ArrayList<>(entry.getValue())); // deep copy das listas
        }
        this.databaseUrl = databaseUrl;
        this.encryptionKey = encryptionKey;
        this.passwordLength = passwordLength;
    }

    public Map<String, List<AppStateManager.AccessInfo>> getState() {
        return state;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public int getPasswordLength() {
        return passwordLength;
    }
}

