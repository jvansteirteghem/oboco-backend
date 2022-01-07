package com.gitlab.jeeto.oboco.common.hash;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.FactoryManager;

public class HashFactory extends Factory {
	private static HashFactory instance;
	private JdkHashFactory jdkHashFactory;
	
	public static HashFactory getInstance() {
		if(instance == null) {
			synchronized(HashFactory.class) {
				if(instance == null) {
					instance = new HashFactory();
					
					FactoryManager factoryManager = FactoryManager.getInstance();
					factoryManager.addFactory(instance);
				}
			}
		}
		return instance;
	}
	
	private HashFactory() {
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
