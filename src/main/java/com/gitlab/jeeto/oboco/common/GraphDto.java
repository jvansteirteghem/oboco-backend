package com.gitlab.jeeto.oboco.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GraphDto {
	private Map<String, GraphDto> nestedGraphMap;
	
	public GraphDto() {
		nestedGraphMap = new LinkedHashMap<String, GraphDto>();
	}
	
	public List<String> getKeys() {
		return new ArrayList<String>(nestedGraphMap.keySet());
	}
	
	public Boolean containsKey(String nestedGraphKey) {
		return nestedGraphMap.containsKey(nestedGraphKey);
	}
	
	public GraphDto get(String nestedGraphKey) {
		return nestedGraphMap.get(nestedGraphKey);
	}
	
	public GraphDto add(String nestedGraphKey, GraphDto nestedGraph) {
		nestedGraphMap.put(nestedGraphKey, nestedGraph);
		
		return this;
	}
	
	public GraphDto remove(String nestedGraphKey) {
		nestedGraphMap.remove(nestedGraphKey);
		
		return this;
	}
}
