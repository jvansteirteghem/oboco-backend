package com.gitlab.jeeto.oboco.common.hash;

import java.io.File;

import com.gitlab.jeeto.oboco.common.FileWrapper;
import com.gitlab.jeeto.oboco.common.hash.HashType;

public interface HashManager {
	public String createHash(FileWrapper<File> inputFileWrapper, HashType outputHashType) throws Exception;
}
