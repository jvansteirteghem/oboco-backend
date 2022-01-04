package com.gitlab.jeeto.oboco.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactoryManager {
	private static Logger logger = LoggerFactory.getLogger(FactoryManager.class.getName());
	private static FactoryManager instance;
	private Map<Class<? extends Factory>, Factory> factoryMap;
	
	public static FactoryManager getInstance() {
		if(instance == null) {
			synchronized(FactoryManager.class) {
				if(instance == null) {
					instance = new FactoryManager();
				}
			}
		}
		return instance;
	}
	
	private FactoryManager() {
		super();
	}
	
	public synchronized void start() {
		logger.info("start factoryManager");
		
		factoryMap = new HashMap<Class<? extends Factory>, Factory>();
	}
	
	public synchronized void stop() {
		logger.info("stop factoryManager");
		
		Iterator<Map.Entry<Class<? extends Factory>, Factory>> iterator = factoryMap.entrySet().iterator();
		while(iterator.hasNext()) {
		    Map.Entry<Class<? extends Factory>, Factory> nextMapEntry = iterator.next();
		    Factory factory = nextMapEntry.getValue();
		    factory.stop();
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Factory> T getFactory(Class<T> factoryClass) throws Exception {
		T factory = (T) factoryMap.get(factoryClass);
		
		if(factory == null) {
			factory = factoryClass.newInstance();
			factory.start();
			
			factoryMap.put(factoryClass, factory);
		}
		
		return factory;
	}
}
