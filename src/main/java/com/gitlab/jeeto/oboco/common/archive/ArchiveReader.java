package com.gitlab.jeeto.oboco.common.archive;

import java.io.File;

import com.gitlab.jeeto.oboco.common.FileWrapper;

public interface ArchiveReader {
	public void openArchive(FileWrapper<File> inputFileWrapper) throws Exception;
	public void closeArchive() throws Exception;
    public FileWrapper<File> readFile(Integer index) throws Exception;
    public Integer readSize() throws Exception;
}
