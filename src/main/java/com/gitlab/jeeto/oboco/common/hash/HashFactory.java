package com.gitlab.jeeto.oboco.common.hash;

import com.gitlab.jeeto.oboco.common.Factory;

public class HashFactory implements Factory {
	private JdkHashFactory jdkHashFactory;
	
	public HashFactory() {
		super();
		
		jdkHashFactory = new JdkHashFactory();
	}
	
	public Hash getHash(HashType hashType) throws Exception {
		return jdkHashFactory.getHash(hashType);
	}

	@Override
	public void start() {
		jdkHashFactory.start();
	}

	@Override
	public void stop() {
		jdkHashFactory.stop();
	}
}
