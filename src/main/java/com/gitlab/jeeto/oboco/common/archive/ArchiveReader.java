package com.gitlab.jeeto.oboco.common.archive;

import com.gitlab.jeeto.oboco.common.TypeableFile;

public interface ArchiveReader {
	public void openArchive(TypeableFile inputFile) throws Exception;
	public void closeArchive() throws Exception;
    public TypeableFile readFile(Integer index) throws Exception;
    public Integer readSize() throws Exception;
}
