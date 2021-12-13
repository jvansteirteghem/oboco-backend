package com.gitlab.jeeto.oboco.data;

import com.gitlab.jeeto.oboco.data.NameHelper;

import junit.framework.TestCase;

public class NameHelperTest extends TestCase {
	public void test() throws Exception {
		String name = "le c\u00F4te d' azure!";
		String normalizedName = NameHelper.getNormalizedName(name);
		
		System.out.println(normalizedName);
	}
	
	public void test2() throws Exception {
		String name = "\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DF";
		String normalizedName = NameHelper.getNormalizedName(name);
		
		System.out.println(normalizedName);
	}
	
	public void test3() throws Exception {
		String name = "a + b = c";
		String normalizedName = NameHelper.getNormalizedName(name);
		
		System.out.println(normalizedName);
	}
	
	public void test4() throws Exception {
		String name = "a@b";
		String normalizedName = NameHelper.getNormalizedName(name);
		
		System.out.println(normalizedName);
	}
}
