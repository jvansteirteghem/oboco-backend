package com.gitlab.jeeto.oboco.common.hash.jdk;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.hash.Hash;
import com.gitlab.jeeto.oboco.common.hash.HashType;
import com.gitlab.jeeto.oboco.common.hash.jdk.JdkHash.JdkSha256Hash;

public class JdkHashFactory extends Factory {
	public JdkHashFactory() {
		super();
	}
	
	public Hash getHash(HashType hashType) throws Exception {
		Hash hash = null;
		
		if(HashType.SHA256.equals(hashType)) {
			hash = new JdkSha256Hash();
		} else {
			throw new Exception("hashType not supported.");
		}
		
		return hash;
	}
}
