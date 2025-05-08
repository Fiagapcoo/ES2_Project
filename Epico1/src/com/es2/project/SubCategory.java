package com.es2.project;

import java.util.ArrayList;
import java.util.List;

public class SubCategory extends Category {
    private List<Category> children = new ArrayList<>();


    public SubCategory(String name,StorageManager storageManager) {
        super(name,storageManager);
    }

    public void addChild(Category child) {
        children.add(child);
    }

    public void removeChild(Category child) {
        children.remove(child);
    }

    @Override
    public void display() {
        System.out.println("Diretorio" + getName() + " | password" + getPassword());


        for (Category child : children) {
            child.display();
        }
    }
}