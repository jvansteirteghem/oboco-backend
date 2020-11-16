package com.gitlab.jeeto.oboco.common.configuration;

import java.io.FileInputStream;
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
					configuration.set("application." + entry.getKey().toString(), entry.getValue().toString());
				}
				
				Properties userProperties = new Properties();
				userProperties.load(new FileInputStream("./user.properties"));
				
				for(Entry<Object, Object> entry: userProperties.entrySet()) {
					configuration.set("user." + entry.getKey().toString(), entry.getValue().toString());
				}
			} catch(Exception e) {
				logger.error("Error.", e);
			}
		}
		
		return configuration;
	}
}
