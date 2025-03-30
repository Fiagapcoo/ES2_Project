package com.es2.project;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.LinkedList;

public class CipherPool {
    private static CipherPool instance;
    private final LinkedList<Cipher> available = new LinkedList<>();
    private final LinkedList<Cipher> inUse = new LinkedList<>();
    private int maxSize = 10;
    private final SecretKeySpec keySpec;

    public CipherPool(String encryptionKey) {
        byte[] keyBytes = encryptionKey.getBytes();
        this.keySpec = new SecretKeySpec(keyBytes, 0, 16, "AES");
    }

    private Cipher createAndInitCipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, keySpec);
        return cipher;
    }

    public static synchronized CipherPool getInstance(String encryptionkey) {
        if (instance == null) {
            instance = new CipherPool(encryptionkey);
        }
        return instance;
    }

    public synchronized Cipher borrowEncryptCipher() throws Exception {
        if (!available.isEmpty()) {
            Cipher cipher = available.removeFirst();
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            inUse.add(cipher);
            return cipher;
        }

        if(inUse.size() < maxSize) {
            Cipher newCipher = createAndInitCipher(Cipher.ENCRYPT_MODE);
            inUse.add(newCipher);
            return newCipher;
        }
        throw new RuntimeException("Pool de Cipher esgotado!");
    }

    public synchronized Cipher borrowDecryptCipher() throws Exception {
        if (!available.isEmpty()) {
            Cipher cipher = available.removeFirst();
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            inUse.add(cipher);
            return cipher;
        }

        if(inUse.size() < maxSize) {
            Cipher newCipher = createAndInitCipher(Cipher.DECRYPT_MODE);
            inUse.add(newCipher);
            return newCipher;
        }
        throw new RuntimeException("Pool de Cipher esgotado!");
    }

    public synchronized void releaseCipher(Cipher cipher) throws Exception{
        if(!inUse.remove(cipher)) {
            throw new RuntimeException("Cipher não pertence ao pool ou já foi libertado.");
        }
        available.add(cipher);
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
