package com.gitlab.jeeto.oboco.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gitlab.jeeto.oboco.data.NaturalOrderComparator;

import junit.framework.TestCase;

public class NaturalOrderComparatorTest extends TestCase {
	@Test
	public void test() {
		List<String> listName = new ArrayList<String>();
		listName.add("bc02p001.jpg");
		listName.add("bc02p000fc-hag.jpg");
		listName.add("bc02p004BC.jpg");
		listName.add("bc02p000ifc.jpg");
		listName.add("bc02p002.jpg");
		listName.add("bc02p003.jpg");
		listName.add("bc02p005.jpg");
		listName.add("ac02p001.jpg");
		listName.add("\u00E0c02p001.jpg");
		
		listName.sort(new NaturalOrderComparator<String>() {
        	@Override
    		public String toString(String o) {
				return o;
        	}
		});
		
		for(String name: listName) {
			System.out.println(name);
		}
	}
	
	@Test
	public void test2() {
		List<String> listName = new ArrayList<String>();
		listName.add("bc02p1.jpg");
		listName.add("bc02p000fc-hag.jpg");
		listName.add("bc02p04BC.jpg");
		listName.add("bc02p00ifc.jpg");
		listName.add("bc02p2.jpg");
		listName.add("bc02p003.jpg");
		listName.add("bc02p05.jpg");
		
		listName.sort(new NaturalOrderComparator<String>() {
        	@Override
    		public String toString(String o) {
				return o;
        	}
		});
		
		for(String name: listName) {
			System.out.println(name);
		}
	}
	
	@Test
	public void test3() {
		List<String> listName = new ArrayList<String>();
		listName.add("bc02p_1.jpg");
		listName.add("bc02p_000_fc-hag.jpg");
		listName.add("bc02p_04_BC.jpg");
		listName.add("bc02p_00_ifc.jpg");
		listName.add("bc02p_2.jpg");
		listName.add("bc02p_003.jpg");
		listName.add("bc02p_05.jpg");
		
		listName.sort(new NaturalOrderComparator<String>() {
        	@Override
    		public String toString(String o) {
				return o;
        	}
		});
		
		for(String name: listName) {
			System.out.println(name);
		}
	}
}
