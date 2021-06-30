package com.gitlab.jeeto.oboco.common;

import junit.framework.TestCase;

public class GraphHelperTest extends TestCase {
	public void test() throws Exception {
		Graph fullGraph = GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration)))");
		Graph graph = GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration)))");
		
		System.out.println("fullGraph: " + GraphHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphHelper.createGraphValue(graph));
		
		GraphHelper.validateGraph(graph, fullGraph);
	}
	
	public void test2() throws Exception {
		Graph fullGraph = GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration)))");
		Graph graph = GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration),partner))");
		
		System.out.println("fullGraph: " + GraphHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphHelper.createGraphValue(graph));
		
		try {
			GraphHelper.validateGraph(graph, fullGraph);
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test3() throws Exception {
		Graph fullGraph = GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration)))");
		Graph graph = GraphHelper.createGraph("(address,employee(address(city),car(brand)))");
		
		System.out.println("fullGraph: " + GraphHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphHelper.createGraphValue(graph));
		
		GraphHelper.validateGraph(graph, fullGraph);
	}
	
	public void test4() throws Exception {
		Graph fullGraph = GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration)))");
		Graph graph = GraphHelper.createGraph("(employee(address(city),car(brand,registration)),address(city))");
		
		System.out.println("fullGraph: " + GraphHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphHelper.createGraphValue(graph));
		
		GraphHelper.validateGraph(graph, fullGraph);
	}
	
	public void test5() throws Exception {
		Graph fullGraph = GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration)))");
		Graph graph = GraphHelper.createGraph("()");
		
		System.out.println("fullGraph: " + GraphHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphHelper.createGraphValue(graph));
		
		GraphHelper.validateGraph(graph, fullGraph);
	}
	
	public void test6() throws Exception {
		Graph fullGraph = GraphHelper.createGraph("()");
		Graph graph = GraphHelper.createGraph("()");
		
		System.out.println("fullGraph: " + GraphHelper.createGraphValue(fullGraph));
		System.out.println("graph: " + GraphHelper.createGraphValue(graph));
		
		GraphHelper.validateGraph(graph, fullGraph);
	}
	
	public void test7() throws Exception {
		try {
			GraphHelper.createGraph("(address(city),employee(address(city),car(brand)registration))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test8() throws Exception {
		try {
			GraphHelper.createGraph("(address(city),employee(address(city),car(brand, registration)))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test9() throws Exception {
		try {
			GraphHelper.createGraph("(address(city),employee(address(city)),car(brand,registration)))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test10() throws Exception {
		try {
			GraphHelper.createGraph("(address(city),employee(address(city),car(brand,registration))))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test11() throws Exception {
		try {
			GraphHelper.createGraph("(address(city),employee(address(city),,car(brand,registration)))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test12() throws Exception {
		try {
			GraphHelper.createGraph("");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test13() throws Exception {
		try {
			GraphHelper.createGraph("()");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test14() throws Exception {
		try {
			GraphHelper.createGraph("(address())");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void test15() throws Exception {
		try {
			GraphHelper.createGraph("(address(()city))");
		} catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
}
