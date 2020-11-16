package com.gitlab.jeeto.oboco.common;

import junit.framework.TestCase;

public class GraphDtoHelperTest extends TestCase {
	public void test() throws Exception {
		GraphDto fullGraph = GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration)))");
		GraphDto graph = GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration)))");
		
		System.out.println("fullGraph: " + GraphDtoHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphDtoHelper.createGraphValue(graph));
		
		GraphDtoHelper.validateGraphDto(graph, fullGraph);
	}
	
	public void test2() throws Exception {
		GraphDto fullGraph = GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration)))");
		GraphDto graph = GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration),partner))");
		
		System.out.println("fullGraph: " + GraphDtoHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphDtoHelper.createGraphValue(graph));
		
		try {
			GraphDtoHelper.validateGraphDto(graph, fullGraph);
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test3() throws Exception {
		GraphDto fullGraph = GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration)))");
		GraphDto graph = GraphDtoHelper.createGraphDto("(address,employee(address(city),car(brand)))");
		
		System.out.println("fullGraph: " + GraphDtoHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphDtoHelper.createGraphValue(graph));
		
		GraphDtoHelper.validateGraphDto(graph, fullGraph);
	}
	
	public void test4() throws Exception {
		GraphDto fullGraph = GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration)))");
		GraphDto graph = GraphDtoHelper.createGraphDto("(employee(address(city),car(brand,registration)),address(city))");
		
		System.out.println("fullGraph: " + GraphDtoHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphDtoHelper.createGraphValue(graph));
		
		GraphDtoHelper.validateGraphDto(graph, fullGraph);
	}
	
	public void test5() throws Exception {
		GraphDto fullGraph = GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration)))");
		GraphDto graph = GraphDtoHelper.createGraphDto("()");
		
		System.out.println("fullGraph: " + GraphDtoHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphDtoHelper.createGraphValue(graph));
		
		GraphDtoHelper.validateGraphDto(graph, fullGraph);
	}
	
	public void test6() throws Exception {
		GraphDto fullGraph = GraphDtoHelper.createGraphDto("()");
		GraphDto graph = GraphDtoHelper.createGraphDto("()");
		
		System.out.println("fullGraph: " + GraphDtoHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphDtoHelper.createGraphValue(graph));
		
		GraphDtoHelper.validateGraphDto(graph, fullGraph);
	}
	
	public void test7() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand)registration))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test8() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand, registration)))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test9() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("(address(city),employee(address(city)),car(brand,registration)))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test10() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("(address(city),employee(address(city),car(brand,registration))))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test11() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("(address(city),employee(address(city),,car(brand,registration)))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test12() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test13() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("()");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test14() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("(address())");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test15() throws Exception {
		try {
			GraphDtoHelper.createGraphDto("(address(()city))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
}
