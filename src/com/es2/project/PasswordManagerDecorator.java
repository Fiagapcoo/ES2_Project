package com.es2.project;

public abstract class PasswordManagerDecorator implements PasswordManager {
    protected PasswordManager wrapped;

    public PasswordManagerDecorator(PasswordManager toDecorate) {
        this.wrapped = toDecorate;
    }

    @Override
    public void savePassword(String category, String password) {
        wrapped.savePassword(category, password);
    }

    @Override
    public String getPassword(String category) {
        return wrapped.getPassword(category);
    }
}