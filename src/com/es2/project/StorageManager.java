package com.es2.project;

public class StorageManager {

    private PasswordStorage passwordStorage;

    public StorageManager(PasswordStorage passwordStorage) {

        this.passwordStorage = passwordStorage;

    }

    public void savePassword(String categoryName, String password) {

        passwordStorage.savePassword(categoryName, password);

    }


    public String loadPassword(String categoryName) {

        return passwordStorage.loadPassword(categoryName);

    }
}