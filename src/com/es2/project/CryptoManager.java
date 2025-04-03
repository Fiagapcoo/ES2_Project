package com.es2.project;

import javax.crypto.Cipher;
import java.util.Base64;

public class CryptoManager {
    private static CryptoManager instance;
    private final CipherPool cipherPool;

    private CryptoManager(String key) {
        cipherPool = CipherPool.getInstance(key);
    }

    public static synchronized CryptoManager getInstance() {
        if (instance == null) {
            instance = new CryptoManager(AppConfig.getInstance().getEncryptionKey());
        }
        return instance;
    }

    public static synchronized void reload(String newKey) {
        CipherPool.reset(newKey);
        instance = new CryptoManager(newKey);
    }

    public String encrypt(String data) {
        Cipher cipher = null;
        try {
            cipher = cipherPool.borrowCipher(Cipher.ENCRYPT_MODE);
            byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encriptar", e);
        } finally {
            try {
                if (cipher != null) cipherPool.releaseCipher(cipher);
            } catch (Exception e) {
                System.err.println("Erro ao libertar cipher: " + e.getMessage());
            }
        }
    }

    public String decrypt(String data) {
        Cipher cipher = null;
        try {
            cipher = cipherPool.borrowCipher(Cipher.DECRYPT_MODE);
            byte[] decoded = Base64.getDecoder().decode(data);
            return new String(cipher.doFinal(decoded), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desencriptar", e);
        } finally {
            try {
                if (cipher != null) cipherPool.releaseCipher(cipher);
            } catch (Exception e) {
                System.err.println("Erro ao libertar cipher: " + e.getMessage());
            }
        }
    }
}
