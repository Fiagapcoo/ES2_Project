package com.es2.project;

public class MFADecorator extends PasswordManagerDecorator {
    public MFADecorator(PasswordManager toDecorate) {
        super(toDecorate);
    }

    @Override
    public void savePassword(String category, String password) {
        if (verifyMFA()) { // Check MFA before saving
            super.savePassword(category, password);
        } else {
            throw new SecurityException("MFA verification failed!");
        }
    }

    private boolean verifyMFA() {
        System.out.println("[MFA] Sending verification code...");
        // Simulate MFA check (e.g., SMS/email code)
        return true; // Assume verification passed
    }
}