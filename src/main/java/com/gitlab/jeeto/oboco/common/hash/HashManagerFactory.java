package com.gitlab.jeeto.oboco.common.hash;

public class HashManagerFactory {
	private static HashManagerFactory instance;
	
	public static HashManagerFactory getInstance() {
		if(instance == null) {
			synchronized(HashManagerFactory.class) {
				if(instance == null) {
					instance = new HashManagerFactory();
				}
			}
		}
		return instance;
	}
	
	private HashManagerFactory() {
		super();
	}
	
	public HashManager getHashManager(HashType outputHashType) throws Exception {
		HashManager hashManager = null;
		
		if(outputHashType.equals(HashType.SHA256)) {
			hashManager = new DefaultHashManager();
		} else {
			throw new Exception("hashType not supported.");
		}
		
		return hashManager;
	}
}
