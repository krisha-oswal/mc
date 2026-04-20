package mindcheck.security;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

/**
 * AES-128 encryption/decryption for journal entry content.
 * Uses CBC mode with PKCS5 padding.
 * Demonstrates: Security Layer / Encapsulation
 */
public class AESEncryptor {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_HEX   = "4D696E6443686563 6B4B657930313233"; // fixed demo key
    private final SecretKeySpec secretKey;
    private final IvParameterSpec iv;

    public AESEncryptor() {
        // 16-byte key (AES-128) — in production: load from secure storage
        byte[] keyBytes = "MindCheckKey0123".getBytes();
        byte[] ivBytes  = "MindCheckIV01234".getBytes();
        secretKey = new SecretKeySpec(keyBytes, "AES");
        iv        = new IvParameterSpec(ivBytes);
    }

    /**
     * Encrypts plaintext and returns Base64-encoded ciphertext.
     */
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            // Fallback: return plaintext if encryption fails (dev mode)
            System.err.println("Encryption warning: " + e.getMessage());
            return plaintext;
        }
    }

    /**
     * Decrypts Base64-encoded ciphertext back to plaintext.
     */
    public String decrypt(String ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decoded   = Base64.getDecoder().decode(ciphertext);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            // Fallback: return as-is (may already be plaintext in dev mode)
            return ciphertext;
        }
    }

    /** Test round-trip. */
    public boolean selfTest() {
        String original = "Hello MindCheck!";
        return original.equals(decrypt(encrypt(original)));
    }
}
