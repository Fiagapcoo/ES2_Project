import com.es2.project.AppConfig;

public class Main {
    public static void main(String[] args) {
        AppConfig config = AppConfig.getInstance();
        //System.out.println("Before Update:");
        System.out.println("Database URL: " + config.getDatabaseUrl());
        System.out.println("Encryption Key: " + config.getEncryptionKey());
        System.out.println("Password Length: " + config.getPasswordLength());

        System.out.println("Alphanumeric Password: " + config.generatePassword("ALPHANUMERIC"));
        System.out.println("Special Character Password: " + config.generatePassword("SPECIAL"));
    }
}
