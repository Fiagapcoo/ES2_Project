import com.es2.project.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Load application configurations
        AppConfig config = AppConfig.getInstance();
        System.out.println("Database URL: " + config.getDatabaseUrl());
        System.out.println("Encryption Key: " + config.getEncryptionKey());
        System.out.println("Password Length: " + config.getPasswordLength());
        System.out.println("File Storage Path: " + config.get_path());

        // Initialize password generators
        PasswordGenerator Alphanumerical = PasswordGeneratorFactory.createGenerator("ALPHANUMERIC");
        PasswordGenerator Special = PasswordGeneratorFactory.createGenerator("SPECIAL");

        // Generate and display passwords
        System.out.println("Alphanumeric Password: " + Alphanumerical.generate(11) );
        System.out.println("Special Character Password: " + Special.generate(11));

        /**
         * Create a password with the configured length (11 characters).
         */
        String password = Alphanumerical.generate(11);

        /**
         * Initialize the file-based password storage interface.
         */
        PasswordStorage fileStorage = new FilePasswordStorage(config.get_path());

        /**
         * Create a storage manager to handle password storage operations.
         */
         StorageManager FilestorageManager = new StorageManager(fileStorage);

        /**
         * Create a parent category for storing student passwords.
         */
        SubCategory escola = new SubCategory("Estudantes", FilestorageManager);
        escola.setPassword(password);

        /**
         * Create a subcategory for storing class-specific passwords.
         */
        SubCategory turmaA = new SubCategory("turmaA", FilestorageManager);
        turmaA.setPassword("turmaA123");

        // Add subcategory to parent
        escola.addChild(turmaA);

        // Display the category hierarchy
        escola.display();

        // Criar sistema de snapshots
        AppStateManager appStateManager = AppStateManager.getInstance();
        AppStateBackupService backupService = new AppStateBackupService(appStateManager);

        // Tirar snapshot após setup inicial
        backupService.takeSnapshot();
        System.out.println("📸 Snapshot 0 guardado.");

        // Simular alterações
        turmaA.setPassword("--------------------------------------------------------------------------");
        config.setPasswordLength(20);
        config.setDatabaseUrl("jdbc:mysql://localhost/testedb");
        config.setEncryptionKey("novaChaveSegura123");

        // Verificar valores depois da alteração
        System.out.println("\n🔧 Estado após alteração:");
        System.out.println("Password turmaA (atual): " + turmaA.getPassword());
        System.out.println("Novo passwordLength: " + config.getPasswordLength());
        System.out.println("Nova DB URL: " + config.getDatabaseUrl());
        System.out.println("Nova EncryptionKey: " + config.getEncryptionKey());

        // Restaurar o snapshot inicial
        try {
            backupService.restoreSnapshot(0, FilestorageManager);
            System.out.println("\n⏪ Estado restaurado para snapshot 0:");
            System.out.println("Password turmaA (restaurada): " + turmaA.getPassword());
            System.out.println("PasswordLength restaurado: " + config.getPasswordLength());
            System.out.println("DB URL restaurada: " + config.getDatabaseUrl());
            System.out.println("EncryptionKey restaurada: " + config.getEncryptionKey());
        } catch (Exception e) {
            System.err.println("Erro ao restaurar snapshot: " + e.getMessage());
        }

        System.out.println("\n✅ Teste completo.");


    }
}
