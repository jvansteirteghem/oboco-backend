package com.gitlab.jeeto.oboco.common.configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
	private Map<String, String> map;
	
	public Configuration() {
		map = new HashMap<String, String>();
	}
	
	public String get(String key) {
		return map.get(key);
	}
	
	public String getAsString(String key, String defaultValue) {
		String value = null;
		
		if(map.containsKey(key)) {
			value = map.get(key);
		} else {
			value = defaultValue;
		}
		
		return value;
	}
	
	public List<String> getAsStringList(String key, String defaultValue) {
		String value = getAsString(key, defaultValue);
		
		if(value == null) {
			return null;
		}
		
		String[] values = value.split("\\s*,\\s*");
		
		return Arrays.asList(values);
	}
	
	public Integer getAsInteger(String key, String defaultValue) {
		String value = getAsString(key, defaultValue);
		
		if(value == null) {
			return null;
		}
		
		return Integer.valueOf(value);
	}
	
	public Long getAsLong(String key, String defaultValue) {
		String value = getAsString(key, defaultValue);
		
		if(value == null) {
			return null;
		}
		
		return Long.valueOf(value);
	}
	
	public Boolean getAsBoolean(String key, String defaultValue) {
		String value = getAsString(key, defaultValue);
		
		if(value == null) {
			return null;
		}
		
		return Boolean.valueOf(value);
	}
	
	public String set(String key, String value) {
		return map.put(key, value);
	}
}
