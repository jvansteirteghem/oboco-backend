package com.gitlab.jeeto.oboco.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Graph {
	private Map<String, Graph> nestedGraphMap;
	
	public Graph() {
		nestedGraphMap = new LinkedHashMap<String, Graph>();
	}
	
	public List<String> getKeys() {
		return new ArrayList<String>(nestedGraphMap.keySet());
	}
	
	public Boolean containsKey(String nestedGraphKey) {
		return nestedGraphMap.containsKey(nestedGraphKey);
	}
	
	public Graph get(String nestedGraphKey) {
		return nestedGraphMap.get(nestedGraphKey);
	}
	
	public Graph add(String nestedGraphKey, Graph nestedGraph) {
		nestedGraphMap.put(nestedGraphKey, nestedGraph);
		
		return this;
	}
	
	public Graph remove(String nestedGraphKey) {
		nestedGraphMap.remove(nestedGraphKey);
		
		return this;
	}
}
