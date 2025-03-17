import com.es2.project.AppConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class AppConfigTest {

    /**
     * Tests whether the singleton pattern is correctly implemented by ensuring
     * that multiple calls to getInstance() return the same instance.
     */
    @Test
    void testSingletonInstanceUnicity() {
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        assertSame(instance1, instance2, "Instances should be the same");
        System.out.println("✅ testSingletonInstanceUnicity passed!");
    }

    /**
     * Tests if the singleton instance is thread-safe by creating multiple threads
     * that access getInstance() concurrently. All instances should be the same.
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
        System.out.println("✅ testThreadSafety passed!");
    }

    /**
     * Tests if an invalid configuration file causes the singleton initialization
     * to throw an exception due to an invalid password length.
     */
    @Test
    void testInvalidPasswordLength() {
        try {
            // Reset the singleton instance using reflection
            java.lang.reflect.Field instanceField = AppConfig.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);

            // Set a system property to use an invalid configuration file
            System.setProperty("config.file", "invalid_config.properties");

            // Singleton initialization should fail with an exception
            assertThrows(IllegalArgumentException.class, AppConfig::getInstance);
            System.out.println("✅ testInvalidPasswordLength passed!");
        } catch (Exception e) {
            fail("Error while resetting singleton: " + e.getMessage());
        } finally {
            // Clear the property to avoid affecting other tests
            System.clearProperty("config.file");
        }
    }
}
