package com.gitlab.jeeto.oboco.data;

import com.ibm.icu.text.Transliterator;

public class NameHelper {
	public static String getNormalizedName(String name) {
		String normalizedName = name;
		
		if (normalizedName != null && "".equalsIgnoreCase(normalizedName) == false) {
			Transliterator transliterator = Transliterator.getInstance("Any-Latin; Latin-ASCII; Lower; [^a-z0-9] Remove;");
			
			normalizedName = transliterator.transliterate(normalizedName);
		}
		
		return normalizedName;
	}
}
