package com.gitlab.jeeto.oboco.common.hash;

import java.io.File;

public interface Hash {
	public String calculate(File inputFile) throws Exception;
	
	public static interface Sha256Hash extends Hash {
		
	}
}
