package com.gitlab.jeeto.oboco.common.archive;

import java.util.Set;

import com.gitlab.jeeto.oboco.common.TypeableFile;

public interface ArchiveReader {
	public void openArchive(TypeableFile inputFile) throws Exception;
	public void closeArchive() throws Exception;
    public TypeableFile getFile(ArchiveReaderEntry archiveReaderEntry) throws Exception;
    public Set<ArchiveReaderEntry> getArchiveReaderEntrySet() throws Exception;
}
