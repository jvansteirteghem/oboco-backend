package com.gitlab.jeeto.oboco.common.configuration;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {
	private static Logger logger = LoggerFactory.getLogger(ConfigurationManager.class.getName());
	private static ConfigurationManager instance;
	private Configuration configuration;
	
	private ConfigurationManager() {
		super();
	}
	
	public static ConfigurationManager getInstance() {
		if(instance == null) {
			synchronized(ConfigurationManager.class) {
				if(instance == null) {
					instance = new ConfigurationManager();
				}
			}
		}
		return instance;
	}
	
	public synchronized Configuration getConfiguration() {
		if(configuration == null) {
			configuration = new Configuration();
			
			try {
				Properties applicationProperties = new Properties();
				applicationProperties.load(new FileInputStream("./application.properties"));
				
				for(Entry<Object, Object> entry: applicationProperties.entrySet()) {
					configuration.set(entry.getKey().toString(), entry.getValue().toString());
				}
				
				Map<String, String> env = System.getenv();
				for(Entry<String, String> entry: env.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					
					if(key.startsWith("APPLICATION_")) {
						key = key.replace("APPLICATION_", "");
						key = key.replace("_", ".");
						key = key.toLowerCase();
						
						configuration.set(key, value);
					}
				}
			} catch(Exception e) {
				logger.error("Error.", e);
			}
		}
		
		return configuration;
	}
}
