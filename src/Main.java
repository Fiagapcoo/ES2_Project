import com.es2.project.AppConfig;
import com.es2.project.AppConfigManager;

public class Main {
    public static void main(String[] args) {
        AppConfig config = AppConfig.getInstance();
        //System.out.println("Before Update:");
        System.out.println("Database URL: " + config.getDatabaseUrl());
        System.out.println("Encryption Key: " + config.getEncryptionKey());
        System.out.println("Password Length: " + config.getPasswordLength());

        /* Updating configurations
        AppConfigManager.setDatabaseUrl("jdbc:mysql://localhost:3306/newdb");
        AppConfigManager.setEncryptionKey("newsecretkey");
        AppConfigManager.setPasswordLength(12);
        */

        /* Uncomment the above lines to update the configurations
        System.out.println("\nAfter Update:");
        System.out.println("Database URL: " + config.getDatabaseUrl());
        System.out.println("Encryption Key: " + config.getEncryptionKey());
        System.out.println("Password Length: " + config.getPasswordLength());
        */
    }
}
