import com.es2.project.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.Properties;
import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AppConfig singleton and related functionality.
 * Includes tests for configuration validation, password generation,
 * Object Pool pattern, Memento pattern implementations, and Decorator pattern.
 */
public class AppConfigTest {
    private String passwordFilePath;

    @BeforeEach
    void resetSingletons() throws Exception {
        // Load the password file path from config.properties
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            passwordFilePath = props.getProperty("folder.path");
        }

        // Reset AppConfig singleton
        var appConfigInstance = AppConfig.class.getDeclaredField("instance");
        appConfigInstance.setAccessible(true);
        appConfigInstance.set(null, null);

        // Reset AppStateManager singleton
        var appStateInstance = AppStateManager.class.getDeclaredField("instance");
        appStateInstance.setAccessible(true);
        appStateInstance.set(null, null);

        // Reset StorageManager singleton
        var storageManagerInstance = StorageManager.class.getDeclaredField("instance");
        storageManagerInstance.setAccessible(true);
        storageManagerInstance.set(null, null);

        // Reset CryptoManager singleton
        var cryptoManagerInstance = CryptoManager.class.getDeclaredField("instance");
        cryptoManagerInstance.setAccessible(true);
        cryptoManagerInstance.set(null, null);

        // Reset CipherPool singleton
        var cipherPoolInstance = CipherPool.class.getDeclaredField("instance");
        cipherPoolInstance.setAccessible(true);
        cipherPoolInstance.set(null, null);

        // Clear the test file before each test
        new File(passwordFilePath).delete();
    }

    /* ============================================= */
    /* ===== EXISTING TESTS (UNCHANGED) ===== */
    /* ============================================= */

    @Test
    void testMementoStateRestoration() throws Exception {
        AppConfig config = AppConfig.getInstance();
        StorageManager storageManager = StorageManager.getInstance();
        SubCategory category = new SubCategory("TestCategory", storageManager);
        category.setPassword("originalPassword");

        AppStateBackupService backup = new AppStateBackupService(AppStateManager.getInstance());
        backup.takeSnapshot();

        category.setPassword("modifiedPassword");
        config.setPasswordLength(20);

        backup.restoreSnapshot(0);

        String restoredPassword = category.getPassword();
        assertEquals("originalPassword", restoredPassword, "Password should be restored correctly after snapshot");
        assertEquals(11, config.getPasswordLength());
    }

    @Test
    void testSingletonInstanceUnicity() {
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        assertSame(instance1, instance2, "Instances should be the same");
    }

    @Test
    void testThreadSafety() throws Exception {
        final int numberOfThreads = 100;
        var executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<AppConfig>> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(AppConfig::getInstance);
        }

        List<Future<AppConfig>> results = executor.invokeAll(tasks);
        AppConfig firstInstance = results.get(0).get();

        for (Future<AppConfig> result : results) {
            assertSame(firstInstance, result.get(), "All instances should be the same");
        }

        executor.shutdown();
    }

    @Test
    void testInvalidPasswordLength() {
        try {
            System.setProperty("config.file", "invalid_config.properties");
            assertThrows(IllegalArgumentException.class, AppConfig::getInstance);
        } catch (Exception e) {
            fail("Error while resetting singleton: " + e.getMessage());
        } finally {
            System.clearProperty("config.file");
        }
    }

    @Test
    void testGenerateSpecialPassword() {
        AppConfig config = AppConfig.getInstance();
        int length = config.getPasswordLength();

        String password = PasswordGeneratorFactory.createGenerator("SPECIAL").generate(length);

        assertNotNull(password);
        assertEquals(length, password.length());
        assertTrue(password.matches("[a-zA-Z0-9!@#$%^&*()_=+\\-]+"));
    }

    @Test
    void testGenerateAlphanumericPassword() {
        AppConfig config = AppConfig.getInstance();
        int length = config.getPasswordLength();

        String password = PasswordGeneratorFactory.createGenerator("ALPHANUMERIC").generate(length);

        assertNotNull(password);
        assertEquals(length, password.length());
        assertTrue(password.matches("^[a-zA-Z0-9]+$"));
    }

    @Test
    void testGenerateInvalidPasswordType() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordGeneratorFactory.createGenerator("INVALID");
        });
    }

    @Test
    void testFolderPathConfiguration() {
        AppConfig config = AppConfig.getInstance();
        assertNotNull(config.get_path());
    }

    @Test
    void testCipherPoolInstanceUnicity() {
        CipherPool pool1 = CipherPool.getInstance("key1-chave-segura-top");
        CipherPool pool2 = CipherPool.getInstance("key1-chave-segura-top");
        assertSame(pool1, pool2);
    }

    @Test
    void testCipherPoolBorrowAndRelease() throws Exception {
        CipherPool pool = CipherPool.getInstance("testKey-chave-segura-top");
        Cipher cipher = pool.borrowCipher(1);
        assertNotNull(cipher);
        pool.releaseCipher(cipher);
    }

    @Test
    void testCipherPoolExhaustion() throws Exception {
        CipherPool.reset("uma-chave-muito-segura");
        CipherPool pool = CipherPool.getInstance("uma-chave-muito-segura");
        pool.setMaxSize(1);

        Cipher first = pool.borrowCipher(Cipher.ENCRYPT_MODE);
        assertThrows(RuntimeException.class, () -> pool.borrowCipher(Cipher.ENCRYPT_MODE));
    }

    @Test
    void testMementoSnapshotIntegrity() {
        AppStateManager manager = AppStateManager.getInstance();
        manager.recordAccess("Category1", "pass1", "modification");
        AppState state = manager.saveState();
        assertTrue(state.getState().containsKey("Category1"));
    }

    @Test
    void testMementoInvalidSnapshot() {
        AppStateBackupService backup = new AppStateBackupService(AppStateManager.getInstance());
        assertThrows(Exception.class, () -> backup.restoreSnapshot(999));
    }

    @Test
    void testKeyChangeTriggersReset() throws Exception {
        AppConfig config = AppConfig.getInstance();
        config.setEncryptionKey("AAAAAAAAAAAAAAAA");
        String password = "superSecreta";

        StorageManager storageManager = StorageManager.getInstance();
        storageManager.savePassword("TestCategory", password);

        String decrypted = storageManager.loadPassword("TestCategory");
        assertEquals(password, decrypted);

        config.setEncryptionKey("ZZZZZZZZZZZZZZZZ");
        String result = storageManager.loadPassword("TestCategory");
        assertNotEquals(password, result, "Decrypted password should not match original after key change");
    }

    /* ============================================= */
    /* ===== NEW DECORATOR PATTERN TESTS ===== */
    /* ============================================= */

    @Test
    void testBasicPasswordManager_WithRealFileStorage() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager manager = new BasicPasswordManager(storageManager);

        manager.savePassword("email", "testPass123");
        assertEquals("testPass123", manager.getPassword("email"));
    }

    @Test
    void testMFADecorator_WithRealImplementation() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager baseManager = new BasicPasswordManager(storageManager);
        PasswordManager securedManager = new MFADecorator(baseManager);

        securedManager.savePassword("bank", "securePassword");
        assertEquals("securePassword", securedManager.getPassword("bank"));
    }

    @Test
    void testSecurityAlertDecorator_WithRealImplementation() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager baseManager = new BasicPasswordManager(storageManager);
        PasswordManager alertedManager = new SecurityAlertDecorator(baseManager);

        alertedManager.savePassword("admin", "adminPass");
        assertEquals("adminPass", alertedManager.getPassword("admin"));
    }

    @Test
    void testDecoratorStack_CombinedFeatures() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager manager = new SecurityAlertDecorator(
                new MFADecorator(
                        new BasicPasswordManager(storageManager)
                )
        );

        manager.savePassword("combined", "test123");
        assertEquals("test123", manager.getPassword("combined"));
    }

    @Test
    void testDecorator_FilePersistence() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager manager = new BasicPasswordManager(storageManager);

        manager.savePassword("persistence", "shouldPersist");

        // Create new instances to verify file persistence
        PasswordStorage newStorage = new FilePasswordStorage(passwordFilePath);
        StorageManager newStorageManager = new StorageManager(newStorage);
        PasswordManager newManager = new BasicPasswordManager(newStorageManager);

        assertEquals("shouldPersist", newManager.getPassword("persistence"));
    }
}