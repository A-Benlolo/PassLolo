package crypto;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	/**
	 * Encrypt a message using AES in CBC mode with PKCS5 Padding
	 * 
	 * @param message The message to encrypt.
	 * @param hashedKey The key to use.
	 * @return The encrypted byte[]
	 * @throws Exception You probably won't ever see this.
	 */
	public static byte[] encrypt(byte[] message, byte[] hashedKey) throws Exception {
		// Generate the iv
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[16];
		random.nextBytes(iv);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		// Create the key
		SecretKeySpec secretKeySpec = new SecretKeySpec(hashedKey, "AES");

		// Encrypt
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encrypted = cipher.doFinal(message);

		// Combine the iv and the encrypted text
		byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
		System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
		System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);

		return encryptedIVAndText;
	}

	/**
	 * Decrypt a message using AES in CBC mode with PKCS5 Padding.
	 * 
	 * @param message The message to decrypt.
	 * @param hashedKey The key to use.
	 * @return The decrypted byte[].
	 * @throws Exception Wrong key.
	 */
	public static byte[] decrypt(byte[] message, byte[] hashedKey) throws Exception {        
		// Extract the iv
		byte[] iv = new byte[16];
		System.arraycopy(message, 0, iv, 0, iv.length);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		// Extract encrypted message
		int encryptedSize = message.length - iv.length;
		byte[] encryptedBytes = new byte[encryptedSize];
		System.arraycopy(message, iv.length, encryptedBytes, 0, encryptedSize);

		// Create the key
		SecretKeySpec secretKeySpec = new SecretKeySpec(hashedKey, "AES");

		// Decrypt
		Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

		return decrypted;
	}

	public static byte[] hashKey(String key) throws Exception{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");		// Get the SHA256 hashing algorithm
		digest.update(key.getBytes("UTF-8"));								// Hash the key
		byte[] keyBytes = new byte[16];										// Prepare the byte array
		System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);	// Copy the 256 bits of the hash into the byte array
		return keyBytes;													// Return the byte array
	}
}