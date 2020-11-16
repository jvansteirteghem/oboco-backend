package com.gitlab.jeeto.oboco.common;

import java.util.StringTokenizer;

import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;

public class GraphDtoHelper {
	public static GraphDto createGraphDto() {
		return new GraphDto();
	}
	
	public static void validateGraphDto(GraphDto graph, GraphDto fullGraph) throws ProblemException {
		if(graph != null) {
			for(String nestedGraphKey: graph.getKeys()) {
				if(fullGraph != null && fullGraph.containsKey(nestedGraphKey)) {
					GraphDto nestedFullGraph = fullGraph.get(nestedGraphKey);
					GraphDto nestedGraph = graph.get(nestedGraphKey);
					
					validateGraphDto(nestedGraph, nestedFullGraph);
				} else {
					throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: " + nestedGraphKey + "."));
				}
			}
		}
	}
	
	// (person(name,birthDate),car)
	public static GraphDto createGraphDto(String value) throws ProblemException {
		if(value == null) {
			return null;
		}
		
		if(value.length() < 2) {
			throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected to start with ( and end with )."));
		}
		
		for(int i = 0; i < value.length(); i = i + 1) {
			int j = (int) value.charAt(i);
			
			// (
			if(i == 0 && j != 40) {
				throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected to start with (."));
			} else {
				// )
				if(i == value.length() && j != 41) {
					throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected to end with )."));
				} else {
					// ( or ) or , or 0-9 or A-Z or a-z
					if((j == 40 || j == 41 || j == 44 || (j >= 48 && j <= 57) || (j >= 65 && j <= 90) || (j >= 97 && j <= 122)) == false) {
						throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected ( or ) or , or 0-9 or A-Z or a-z."));
					}
				}
			}
		}
		
		String tokenizerValue = value.substring(1, value.length() - 1);
		
		if(tokenizerValue.equals("")) {
			return null;
		}
		
		GraphDto graph = createGraphDto();
		
        StringTokenizer tokenizer = new StringTokenizer(tokenizerValue, "(),", true);
        
        String nestedGraphKey = null;
        String nestedGraphValue = null;
        while (tokenizer.hasMoreElements()) {
            String token = (String) tokenizer.nextElement();
            
            if (token.equals("(")) {
            	nestedGraphValue = "(";
            	
                int i = 1;
                
                while (tokenizer.hasMoreTokens()) {
                	token = (String) tokenizer.nextElement();
                	
                	if(token.equals("(")) {
                		i = i + 1;
                	} else {
	                	if(token.equals(")")) {
	                		i = i - 1;
	                	}
                	}
                	
                	nestedGraphValue = nestedGraphValue + token;
                	
                	if(i == 0) {
                		break;
                	}
                }
                
                if(i != 0) {
                	throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: the number of ( is not equal to the number of )."));
                }
            } else if(token.equals(",")) {
            	// ,
            	if(nestedGraphKey == null && nestedGraphValue == null) {
            		throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected characters before ,."));
            	}
            	// (name),
            	if(nestedGraphKey == null && nestedGraphValue != null) {
            		throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected characters before (."));
            	}
            	
            	// person(name,birthDate),
            	graph.add(nestedGraphKey, createGraphDto(nestedGraphValue));
            	
            	nestedGraphKey = null;
            	nestedGraphValue = null;
            } else {
            	// person(name)birthDate
            	if(nestedGraphValue != null) {
            		throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: unexpected characters after )."));
            	}
            	
            	nestedGraphKey = token;
            }
        }
        
        // EOL
    	if(nestedGraphKey == null && nestedGraphValue == null) {
    		throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected characters before EOL."));
    	}
    	// (name)EOL
    	if(nestedGraphKey == null && nestedGraphValue != null) {
    		throw new ProblemException(new Problem(400, "PROBLEM_GRAPH_INVALID", "The graph is invalid: expected characters before (."));
    	}
    	
    	// person(name,birthDate)EOL
    	graph.add(nestedGraphKey, createGraphDto(nestedGraphValue));
    	
    	nestedGraphKey = null;
    	nestedGraphValue = null;
		
		return graph;
	}
	
	public static String createGraphValue(GraphDto graphDto) {
		String value = "";
		
		if(graphDto != null) {
			for(String nestedGraphKey: graphDto.getKeys()) {
				GraphDto nestedGraph = graphDto.get(nestedGraphKey);
				
				String nestedGraphValue = "";
				if(nestedGraph != null) {
					nestedGraphValue = createGraphValue(nestedGraph);
				}
				
				if(value.equals("")) {
					value = nestedGraphKey + nestedGraphValue;
				} else {
					value = value + "," + nestedGraphKey + nestedGraphValue;
				}
			}
		}
		
		value = "(" + value + ")";
		
		return value;
	}
}
