package com.gitlab.jeeto.oboco.common.hash.jdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import com.gitlab.jeeto.oboco.common.hash.Hash;
import com.gitlab.jeeto.oboco.common.hash.Hash.Sha256Hash;

public abstract class JdkHash implements Hash {
	public abstract String getAlgorithm();
	
	public String formatHash(byte[] hash) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hash.length; i = i + 1) {
			sb.append(String.format("%02x", hash[i]));
		}
		return sb.toString();
	}

	@Override
	public String calculate(File inputFile) throws Exception {
		MessageDigest md = MessageDigest.getInstance(getAlgorithm());
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(inputFile);
			
			byte[] buffer = new byte[8 * 1024];
			int bufferSize;
			while ((bufferSize = inputStream.read(buffer)) != -1) {
				md.update(buffer, 0, bufferSize);
			}
		} finally {
			if(inputStream != null) {
				try {
					inputStream.close();
				} catch(Exception e) {
					// pass
				}
			}
		}
		
		return formatHash(md.digest());
	}
	
	public static class JdkSha256Hash extends JdkHash implements Sha256Hash {
		public String getAlgorithm() {
			return "SHA-256";
		}
	}
}
