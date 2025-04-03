import com.es2.project.*;

public class Main {
    public static void main(String[] args) {
        // Carregar configurações
        AppConfig config = AppConfig.getInstance();
        System.out.println("Database URL: " + config.getDatabaseUrl());
        System.out.println("Encryption Key: " + config.getEncryptionKey());
        System.out.println("Password Length: " + config.getPasswordLength());
        System.out.println("File Storage Path: " + config.get_path());

        // Inicializar geradores de senha
        PasswordGenerator alphanumeric = PasswordGeneratorFactory.createGenerator("ALPHANUMERIC");
        PasswordGenerator special = PasswordGeneratorFactory.createGenerator("SPECIAL");

        // Gerar e exibir senhas
        System.out.println("Alphanumeric Password: " + alphanumeric.generate(11));
        System.out.println("Special Character Password: " + special.generate(11));

        // Usar StorageManager Singleton
        PasswordStorage fileStorage = new FilePasswordStorage(config.get_path());
        StorageManager storageManager = StorageManager.getInstance();

        // Criar hierarquia de categorias
        SubCategory escola = new SubCategory("Estudantes", storageManager);
        escola.setPassword(alphanumeric.generate(11));

        SubCategory turmaA = new SubCategory("turmaA", storageManager);
        turmaA.setPassword("turmaA123");
        escola.addChild(turmaA);

        // Exibir estrutura
        escola.display();

        // Sistema de snapshots
        AppStateManager appStateManager = AppStateManager.getInstance();
        AppStateBackupService backupService = new AppStateBackupService(appStateManager);
        backupService.takeSnapshot();
        System.out.println("\nSnapshot 0 guardado.");

        // Simular alterações
        config.setEncryptionKey("novaChaveSegura123");
        turmaA.setPassword("--------------------------------------------------------------------------");
        config.setPasswordLength(20);
        config.setDatabaseUrl("jdbc:mysql://localhost/testedb");

        System.out.println("\nEstado após alteração:");
        System.out.println("Password turmaA (atual): " + turmaA.getPassword());
        System.out.println("Novo passwordLength: " + config.getPasswordLength());
        System.out.println("Nova DB URL: " + config.getDatabaseUrl());
        System.out.println("Nova EncryptionKey: " + config.getEncryptionKey());

        // Restaurar snapshot
        try {
            backupService.restoreSnapshot(0); // Sem parâmetro StorageManager
            System.out.println("\nEstado restaurado para snapshot 0:");
            System.out.println("Password turmaA (restaurada): " + turmaA.getPassword());
            System.out.println("PasswordLength restaurado: " + config.getPasswordLength());
            System.out.println("DB URL restaurada: " + config.getDatabaseUrl());
            System.out.println("EncryptionKey restaurada: " + config.getEncryptionKey());
        } catch (Exception e) {
            System.err.println("Erro ao restaurar snapshot: " + e.getMessage());
        }

        System.out.println("\nTeste completo.");
    }
}