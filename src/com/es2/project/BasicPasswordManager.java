package com.es2.project;

public class BasicPasswordManager implements PasswordManager {
    private StorageManager storage;

    public BasicPasswordManager(StorageManager storage) {
        this.storage = storage;
    }

    @Override
    public void savePassword(String category, String password) {
        storage.savePassword(category, password);
    }

    @Override
    public String getPassword(String category) {
        return storage.loadPassword(category);
    }
}