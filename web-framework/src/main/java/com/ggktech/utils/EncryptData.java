package com.ggktech.utils;

import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class EncryptData {

	private static final Logger LOGGER = Logger.getLogger(ConsolidateJsonAndSendEmail.class.getName());

	static String encryptionPassword = "Password123";
	
	public static void main(String args[]) {

		EncryptData objEncryptData = new EncryptData();
		String stringToBeEncrypted = "Hyderabad007";
		String encryptedValue = "";

		/** Enter the string which you want to encrypt below */
		encryptedValue = objEncryptData.encryptData(stringToBeEncrypted, encryptionPassword);
		System.out.println("Encrypted Value = " + encryptedValue);

	}

	/**
	 * This method will give the encrypted value for the given input based on the
	 * password
	 * 
	 * @param dataToBeEncrypted
	 *            : String to be encrypted
	 * @param encryptionPassword
	 *            : Password for encryption. Same password need to be used for
	 *            decrypting the result.
	 * @return : {@code String} encrypted value of the given string
	 */
	public String encryptData(String dataToBeEncrypted, String encryptionPassword) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(encryptionPassword);
		return encryptor.encrypt(dataToBeEncrypted);
	}

	/**
	 * This method will decrypt the value based on the encryption password.
	 * 
	 * @param encryptPassword
	 *            : Password which is used to encrypt the data.
	 * @param encryptedValue
	 *            : Encrypted value
	 * @return : {@code String} if correct encrypt password is given.<br>
	 *         {@code NULL} if incorrect encrypt password is given.
	 * 
	 */
	public String getDecryptValue(String encryptedValue) {
		try {
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setPassword(encryptionPassword);
			return encryptor.decrypt(encryptedValue);
		} catch (Exception exp) {
			LOGGER.error("Please enter a valid encryption password to decrypt value.");
		}
		return null;
	}
}
