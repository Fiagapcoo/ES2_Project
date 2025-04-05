import com.es2.project.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
 * Object Pool pattern, and Memento pattern implementations.
 */
public class AppConfigTest {

    @BeforeEach
    void resetSingletons() throws Exception {
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
    }


    /**
     * Tests full Memento pattern workflow.
     * Creates initial state, modifies configuration and passwords,
     * then verifies successful restoration from snapshot.
     */
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


    /**
     * Tests that the AppConfig singleton instance is unique.
     * Verifies that multiple calls to getInstance() return the same object.
     */
    @Test
    void testSingletonInstanceUnicity() {
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        assertSame(instance1, instance2, "Instances should be the same");
    }

    /**
     * Tests thread safety of the AppConfig singleton.
     * Creates 100 concurrent threads accessing getInstance() and verifies
     * all receive the same instance.
     */
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

    /**
     * Tests validation of invalid password length configuration.
     * Loads a configuration file with password length below minimum requirement
     * and expects an IllegalArgumentException.
     */
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

    /**
     * Tests generation of passwords with special characters.
     * Verifies generated password length and character composition.
     */
    @Test
    void testGenerateSpecialPassword() {
        AppConfig config = AppConfig.getInstance();
        int length = config.getPasswordLength();

        String password = PasswordGeneratorFactory.createGenerator("SPECIAL").generate(length);

        assertNotNull(password);
        assertEquals(length, password.length());
        assertTrue(password.matches("[a-zA-Z0-9!@#$%^&*()_=+\\-]+"));
    }

    /**
     * Tests generation of alphanumeric passwords.
     * Verifies generated password contains only letters and numbers.
     */
    @Test
    void testGenerateAlphanumericPassword() {
        AppConfig config = AppConfig.getInstance();
        int length = config.getPasswordLength();

        String password = PasswordGeneratorFactory.createGenerator("ALPHANUMERIC").generate(length);

        assertNotNull(password);
        assertEquals(length, password.length());
        assertTrue(password.matches("^[a-zA-Z0-9]+$"));
    }

    /**
     * Tests handling of invalid password generator type.
     * Expects IllegalArgumentException when requesting non-existent generator.
     */
    @Test
    void testGenerateInvalidPasswordType() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordGeneratorFactory.createGenerator("INVALID");
        });
    }

    /**
     * Tests folder path configuration loading.
     * Verifies the folder path is properly loaded from config file.
     */
    @Test
    void testFolderPathConfiguration() {
        AppConfig config = AppConfig.getInstance();
        assertNotNull(config.get_path());
    }

    /**
     * Tests CipherPool singleton behavior.
     * Verifies that multiple calls with same parameters return same instance.
     */
    @Test
    void testCipherPoolInstanceUnicity() {
        CipherPool pool1 = CipherPool.getInstance("key1-chave-segura-top");
        CipherPool pool2 = CipherPool.getInstance("key1-chave-segura-top");
        assertSame(pool1, pool2);
    }

    /**
     * Tests basic Object Pool functionality.
     * Verifies successful borrowing and releasing of Cipher objects.
     */
    @Test
    void testCipherPoolBorrowAndRelease() throws Exception {
        CipherPool pool = CipherPool.getInstance("testKey-chave-segura-top");
        Cipher cipher = pool.borrowCipher(1);
        assertNotNull(cipher);
        pool.releaseCipher(cipher);
    }

    /**
     * Tests Object Pool capacity limits.
     * Sets maximum pool size to 1 and verifies proper exhaustion behavior.
     */
    @Test
    void testCipherPoolExhaustion() throws Exception {
        CipherPool.reset("uma-chave-muito-segura");
        CipherPool pool = CipherPool.getInstance("uma-chave-muito-segura");
        pool.setMaxSize(1);

        Cipher first = pool.borrowCipher(Cipher.ENCRYPT_MODE); // funciona
        assertThrows(RuntimeException.class, () -> pool.borrowCipher(Cipher.ENCRYPT_MODE));
    }


    /**
     * Tests Memento data integrity.
     * Verifies that created snapshots contain recorded state information.
     */
    @Test
    void testMementoSnapshotIntegrity() {
        AppStateManager manager = AppStateManager.getInstance();
        manager.recordAccess("Category1", "pass1", "modification");
        AppState state = manager.saveState();
        assertTrue(state.getState().containsKey("Category1"));
    }

    /**
     * Tests handling of invalid snapshot restoration.
     * Attempts to restore non-existent snapshot and expects exception.
     */
    @Test
    void testMementoInvalidSnapshot() {
        AppStateBackupService backup = new AppStateBackupService(AppStateManager.getInstance());
        assertThrows(Exception.class, () -> backup.restoreSnapshot(999));
    }

    @Test
    void testKeyChangeTriggersReset() throws Exception {
        AppConfig config = AppConfig.getInstance();
        config.setEncryptionKey("AAAAAAAAAAAAAAAA"); // 16 caracteres bem definidos
        String password = "superSecreta";

        StorageManager storageManager = StorageManager.getInstance();
        storageManager.savePassword("TestCategory", password);

        String decrypted = storageManager.loadPassword("TestCategory");
        assertEquals(password, decrypted); // deve funcionar com a chave correta

        // Muda a chave para uma chave completamente diferente nos primeiros 16 bytes
        config.setEncryptionKey("ZZZZZZZZZZZZZZZZ");

        String result = storageManager.loadPassword("TestCategory");
        assertNotEquals(password, result, "Decrypted password should not match original after key change");

    }



}