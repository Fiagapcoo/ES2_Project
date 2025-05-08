package com.es2.project;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedList;

public class CipherPool {
    private static CipherPool instance;
    private final LinkedList<Cipher> available = new LinkedList<>();
    private final LinkedList<Cipher> inUse = new LinkedList<>();
    private int maxSize = 10;
    private final SecretKeySpec keySpec;

    private CipherPool(String encryptionKey) {
        try {
            byte[] keyBytes = encryptionKey.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes); // gera 32 bytes
            this.keySpec = new SecretKeySpec(keyBytes, 0, 16, "AES"); // usa 128 bits (16 bytes)
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar chave AES", e);
        }
    }

    public static synchronized CipherPool getInstance(String encryptionKey) {
        if (instance == null) {
            instance = new CipherPool(encryptionKey);
        }
        return instance;
    }

    public static synchronized void reset(String encryptionKey) {
        instance = new CipherPool(encryptionKey);
    }

    private Cipher createAndInitCipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(mode, keySpec);
        return cipher;
    }

    public synchronized Cipher borrowCipher(int mode) throws Exception {
        if (!available.isEmpty()) {
            Cipher cipher = available.removeFirst();
            cipher.init(mode, keySpec); // reinit para o modo correto
            inUse.add(cipher);
            return cipher;
        }

        if (inUse.size() < maxSize) {
            Cipher newCipher = createAndInitCipher(mode);
            inUse.add(newCipher);
            return newCipher;
        }
        throw new RuntimeException("Cipher pool exhausted!");
    }

    public synchronized void releaseCipher(Cipher cipher) throws Exception {
        if (!inUse.remove(cipher)) {
            throw new RuntimeException("Cipher não pertence ao pool");
        }
        available.add(cipher);
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
