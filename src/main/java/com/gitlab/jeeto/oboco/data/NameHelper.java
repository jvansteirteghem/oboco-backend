package com.gitlab.jeeto.oboco.data;

import java.io.File;

import com.ibm.icu.text.Transliterator;

public class NameHelper {
	public static String getName(File file) {
		String name = file.getName();
		
		if(file.isFile()) {
			int index = name.lastIndexOf('.');
			if(index != -1) {
				name = name.substring(0, index);
			}
		}
		
		return name;
	}
	
	public static String getNormalizedName(String name) {
		String normalizedName = name;
		
		if (normalizedName != null && "".equalsIgnoreCase(normalizedName) == false) {
			Transliterator transliterator = Transliterator.getInstance("Any-Latin; Latin-ASCII; Lower; [^a-z0-9] Remove;");
			
			normalizedName = transliterator.transliterate(normalizedName);
		}
		
		return normalizedName;
	}
}
