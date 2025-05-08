package com.es2.project;


public interface PasswordManager {
    void savePassword(String category, String password);
    String getPassword(String category);
}