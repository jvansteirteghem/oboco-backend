package com.gitlab.jeeto.oboco.common;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactoryManager {
	private static Logger logger = LoggerFactory.getLogger(FactoryManager.class.getName());
	private static FactoryManager instance;
	private List<Factory> factoryList;
	
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
		
		factoryList = new ArrayList<Factory>();
	}
	
	public synchronized void addFactory(Factory factory) {
		factory.start();
		
		factoryList.add(factory);
	}
	
	public synchronized void start() {
		logger.info("start factoryManager");
		
		for(Factory factory: factoryList) {
			factory.start();
		}
	}
	
	public synchronized void stop() {
		logger.info("stop factoryManager");
		
		for(Factory factory: factoryList) {
			factory.stop();
		}
	}
}
