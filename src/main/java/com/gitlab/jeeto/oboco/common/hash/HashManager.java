package com.gitlab.jeeto.oboco.common.hash;

import com.gitlab.jeeto.oboco.common.TypeableFile;

public interface HashManager {
	public String createHash(TypeableFile inputFile, HashType outputHashType) throws Exception;
}
