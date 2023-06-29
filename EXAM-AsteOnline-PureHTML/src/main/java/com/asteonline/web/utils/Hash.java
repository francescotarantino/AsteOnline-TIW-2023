package com.asteonline.web.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Hash {
	/**
	 * This method is used to get the hash of a given string using SHA-512 algorithm.
	 * Ideally, SHA algorithm family shouldn't be used for hashing password since SHA hashes can be
	 * easily bruteforced.
	 * @param password the string to be hashed.
	 * @return a 128-characters string representing the hash of the given input.
	 */
	public static String hashPassword(String password) {
		String hash;
		
        try {
        	// Get a message digest instance using SHA-512 algorithm
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			
			// Encode the password into a sequence of bytes and pass them to the MessageDigest instance
			md.update(password.getBytes(StandardCharsets.UTF_8));
			
			// Compute the hash
	        byte[] bytes = md.digest();
	        
	        // Convert the bytes into a string
	        StringBuilder sb = new StringBuilder();
			for (byte aByte : bytes) {
				sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
			}

	        hash = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
        
        return hash;
	}
}
