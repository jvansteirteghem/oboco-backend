package com.gitlab.jeeto.oboco.common.hash.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.hash.HashManager;
import com.gitlab.jeeto.oboco.common.hash.HashType;

public class HashManagerImpl implements HashManager {
	@Override
	public String createHash(TypeableFile inputFile, HashType outputHashType) throws Exception {
		MessageDigest md = null;
		
		if(HashType.SHA256.equals(outputHashType)) {
			md = MessageDigest.getInstance("SHA-256");
		}
		
		if(md == null) {
			throw new Exception("extension not found.");
		}
		
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
		
		byte[] hash = md.digest();
		
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < hash.length; i = i + 1) {
            sb.append(String.format("%02x", hash[i]));
        }
        return sb.toString();
	}
}
