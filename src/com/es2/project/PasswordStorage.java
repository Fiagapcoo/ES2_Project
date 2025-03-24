package com.es2.project;

public interface PasswordStorage {

    void savePassword(String categoryName, String password);

    String loadPassword(String categoryName);
}