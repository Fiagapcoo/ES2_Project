package com.es2.project;

public abstract class Category {


    private String name;
    private StorageManager storageManager;


    public Category(String name, StorageManager storageManager) {
        this.name = name;
        this.storageManager = storageManager;
    }

    //nome de identificacao
    public String getName() {
        return name;
    }


    //Bridge
    public void setPassword(String password) {
        storageManager.savePassword(name, password);
    }

    //bridge
    public String getPassword() {
        return storageManager.loadPassword(name);
    }


    //composite
    public abstract void display();
}