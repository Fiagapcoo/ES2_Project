package com.es2.project;

public class LeafCategory extends Category {
    public LeafCategory(String name, StorageManager storageManager) {
        super(name, storageManager);
    }

    @Override
    public void display() {
        System.out.println("Leaf " + getName() + " | password " + getPassword());
    }
}

