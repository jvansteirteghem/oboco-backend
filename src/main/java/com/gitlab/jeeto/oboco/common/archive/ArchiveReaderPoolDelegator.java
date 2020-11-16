package com.gitlab.jeeto.oboco.common.archive;

import java.io.File;

import com.gitlab.jeeto.oboco.common.FileWrapper;

public class ArchiveReaderPoolDelegator implements ArchiveReader {
	private ArchiveReaderPool archiveReaderPool;
	private ArchiveReader archiveReader;
	private String archivePath;
	
	public ArchiveReaderPoolDelegator(ArchiveReaderPool archiveReaderPool, ArchiveReader archiveReader) {
		this.archiveReaderPool = archiveReaderPool;
		this.archiveReader = archiveReader;
	}

	@Override
	public void openArchive(FileWrapper<File> inputFileWrapper) throws Exception {
		archivePath = inputFileWrapper.getFile().getPath();
		
		ArchiveReader archiveReader = archiveReaderPool.removeArchiveReader(archivePath);
		if(archiveReader != null) {
			this.archiveReader = archiveReader;
		} else {
			this.archiveReader.openArchive(inputFileWrapper);
		}
	}

	@Override
	public void closeArchive() throws Exception {
		archiveReaderPool.addArchiveReader(archivePath, archiveReader);
	}

	@Override
	public FileWrapper<File> readFile(Integer index) throws Exception {
		return archiveReader.readFile(index);
	}

	@Override
	public Integer readSize() throws Exception {
		return archiveReader.readSize();
	}

}
