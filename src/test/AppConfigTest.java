import com.es2.project.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.Properties;
import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class AppConfigTest {
    @TempDir
    static Path sharedTempDir;
    private static final AtomicInteger testCounter = new AtomicInteger(1);
    private String passwordFilePath;

    @BeforeEach
    void resetSingletons() throws Exception {
        // Create unique password file for each test
        int testNumber = testCounter.getAndIncrement();
        passwordFilePath = sharedTempDir.resolve("passwords_" + testNumber + ".txt").toString();

        // Load configuration
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            // Override the path with our test-specific path
            props.setProperty("folder.path", passwordFilePath);
        }

        // Reset all singletons
        resetSingleton(AppConfig.class, "instance");
        resetSingleton(AppStateManager.class, "instance");
        resetSingleton(StorageManager.class, "instance");
        resetSingleton(CryptoManager.class, "instance");
        resetSingleton(CipherPool.class, "instance");
    }

    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        var field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    void test1_MementoStateRestoration() throws Exception {
        AppConfig config = AppConfig.getInstance();
        StorageManager storageManager = StorageManager.getInstance();
        SubCategory category = new SubCategory("TestCategory", storageManager);
        category.setPassword("originalPassword");

        AppStateBackupService backup = new AppStateBackupService(AppStateManager.getInstance());
        backup.takeSnapshot();

        category.setPassword("modifiedPassword");
        config.setPasswordLength(20);

        backup.restoreSnapshot(0);

        assertEquals("originalPassword", category.getPassword());
        assertEquals(11, config.getPasswordLength());
    }

    @Test
    void test2_SingletonInstanceUnicity() {
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void test3_ThreadSafety() throws Exception {
        final int numberOfThreads = 100;
        var executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<AppConfig>> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(AppConfig::getInstance);
        }

        List<Future<AppConfig>> results = executor.invokeAll(tasks);
        AppConfig firstInstance = results.get(0).get();

        for (Future<AppConfig> result : results) {
            assertSame(firstInstance, result.get());
        }

        executor.shutdown();
    }

    @Test
    void test4_InvalidPasswordLength() {
        try {
            System.setProperty("config.file", "invalid_config.properties");
            assertThrows(IllegalArgumentException.class, AppConfig::getInstance);
        } finally {
            System.clearProperty("config.file");
        }
    }

    @Test
    void test5_GenerateSpecialPassword() {
        AppConfig config = AppConfig.getInstance();
        int length = config.getPasswordLength();

        String password = PasswordGeneratorFactory.createGenerator("SPECIAL").generate(length);

        assertNotNull(password);
        assertEquals(length, password.length());
        assertTrue(password.matches("[a-zA-Z0-9!@#$%^&*()_=+\\-]+"));
    }

    @Test
    void test6_GenerateAlphanumericPassword() {
        AppConfig config = AppConfig.getInstance();
        int length = config.getPasswordLength();

        String password = PasswordGeneratorFactory.createGenerator("ALPHANUMERIC").generate(length);

        assertNotNull(password);
        assertEquals(length, password.length());
        assertTrue(password.matches("^[a-zA-Z0-9]+$"));
    }

    @Test
    void test7_GenerateInvalidPasswordType() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordGeneratorFactory.createGenerator("INVALID");
        });
    }

    @Test
    void test8_FolderPathConfiguration() {
        AppConfig config = AppConfig.getInstance();
        assertNotNull(config.get_path());
    }

    @Test
    void test9_CipherPoolInstanceUnicity() {
        CipherPool pool1 = CipherPool.getInstance("key1-chave-segura-top");
        CipherPool pool2 = CipherPool.getInstance("key1-chave-segura-top");
        assertSame(pool1, pool2);
    }

    @Test
    void test10_CipherPoolBorrowAndRelease() throws Exception {
        CipherPool pool = CipherPool.getInstance("testKey-chave-segura-top");
        Cipher cipher = pool.borrowCipher(1);
        assertNotNull(cipher);
        pool.releaseCipher(cipher);
    }

    @Test
    void test11_CipherPoolExhaustion() throws Exception {
        CipherPool.reset("uma-chave-muito-segura");
        CipherPool pool = CipherPool.getInstance("uma-chave-muito-segura");
        pool.setMaxSize(1);

        Cipher first = pool.borrowCipher(Cipher.ENCRYPT_MODE);
        assertThrows(RuntimeException.class, () -> pool.borrowCipher(Cipher.ENCRYPT_MODE));
    }

    @Test
    void test12_MementoSnapshotIntegrity() {
        AppStateManager manager = AppStateManager.getInstance();
        manager.recordAccess("Category1", "pass1", "modification");
        AppState state = manager.saveState();
        assertTrue(state.getState().containsKey("Category1"));
    }

    @Test
    void test13_MementoInvalidSnapshot() {
        AppStateBackupService backup = new AppStateBackupService(AppStateManager.getInstance());
        assertThrows(Exception.class, () -> backup.restoreSnapshot(999));
    }

    @Test
    void test14_KeyChangeTriggersReset() throws Exception {
        AppConfig config = AppConfig.getInstance();
        config.setEncryptionKey("AAAAAAAAAAAAAAAA");
        String password = "superSecreta";

        StorageManager storageManager = StorageManager.getInstance();
        storageManager.savePassword("TestCategory", password);

        String decrypted = storageManager.loadPassword("TestCategory");
        assertEquals(password, decrypted);

        config.setEncryptionKey("ZZZZZZZZZZZZZZZZ");
        String result = storageManager.loadPassword("TestCategory");
        assertNotEquals(password, result);
    }

    @Test
    void test15_BasicPasswordManager_WithRealFileStorage() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager manager = new BasicPasswordManager(storageManager);

        manager.savePassword("email", "testPass123");
        assertEquals("testPass123", manager.getPassword("email"));
    }

    @Test
    void test16_MFADecorator_WithRealImplementation() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager baseManager = new BasicPasswordManager(storageManager);
        PasswordManager securedManager = new MFADecorator(baseManager);

        securedManager.savePassword("bank", "securePassword");
        assertEquals("securePassword", securedManager.getPassword("bank"));
    }

    @Test
    void test17_SecurityAlertDecorator_WithRealImplementation() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager baseManager = new BasicPasswordManager(storageManager);
        PasswordManager alertedManager = new SecurityAlertDecorator(baseManager);

        alertedManager.savePassword("admin", "adminPass");
        assertEquals("adminPass", alertedManager.getPassword("admin"));
    }

    @Test
    void test18_DecoratorStack_CombinedFeatures() {
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
    void test19_Decorator_FilePersistence() {
        PasswordStorage storage = new FilePasswordStorage(passwordFilePath);
        StorageManager storageManager = new StorageManager(storage);
        PasswordManager manager = new BasicPasswordManager(storageManager);

        manager.savePassword("persistence", "shouldPersist");

        PasswordStorage newStorage = new FilePasswordStorage(passwordFilePath);
        StorageManager newStorageManager = new StorageManager(newStorage);
        PasswordManager newManager = new BasicPasswordManager(newStorageManager);

        assertEquals("shouldPersist", newManager.getPassword("persistence"));
    }

    @AfterAll
    static void cleanup() {
        testCounter.set(1); // Reset counter for next test class
    }
}