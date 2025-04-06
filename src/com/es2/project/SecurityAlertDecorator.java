package com.es2.project;

public class SecurityAlertDecorator extends PasswordManagerDecorator {
    public SecurityAlertDecorator(PasswordManager toDecorate) {
        super(toDecorate);
    }

    @Override
    public void savePassword(String category, String password) {
        super.savePassword(category, password);
        triggerAlert("Password modified in category: " + category);
    }

    @Override
    public String getPassword(String category) {
        triggerAlert("Password accessed in category: " + category);
        return super.getPassword(category);
    }

    private void triggerAlert(String message) {
        System.out.println("[SECURITY ALERT] " + message);
    }
}