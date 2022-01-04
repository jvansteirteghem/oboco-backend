package com.gitlab.jeeto.oboco.common.hash;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.hash.JdkHash.JdkSha256Hash;

public class JdkHashFactory implements Factory {
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

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		
	}
}
