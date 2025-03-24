package com.es2.project;

import java.io.*;
import java.util.Properties;

public class FilePasswordStorage implements PasswordStorage {
    private final String filePath;

    public FilePasswordStorage(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void savePassword(String categoryName, String password) {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            props.load(input);
        } catch (IOException e) {
            }



        props.setProperty(categoryName, password);

        try (OutputStream output = new FileOutputStream(filePath)) {
            props.store(output, "Passwords das Categorias");
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar password no arquivo", e);
        }
    }

    @Override
    public String loadPassword(String categoryName) {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            props.load(input);
            return props.getProperty(categoryName);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar password do arquivo", e);
        }
    }
}