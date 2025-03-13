import com.es2.project.AppConfig;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        AppConfig config = AppConfig.getInstance();
        System.out.println("Database URL: " + config.getDatabaseUrl());
        System.out.println("Encryption Key: " + config.getEncryptionKey());
        System.out.println("Password Length: " + config.getPasswordLength());
    }
}