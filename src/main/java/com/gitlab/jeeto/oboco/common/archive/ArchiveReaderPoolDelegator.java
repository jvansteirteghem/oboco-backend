package com.gitlab.jeeto.oboco.common.archive;

import com.gitlab.jeeto.oboco.common.TypeableFile;

public class ArchiveReaderPoolDelegator implements ArchiveReader {
	private ArchiveReaderPool archiveReaderPool;
	private ArchiveReader archiveReader;
	private String archivePath;
	
	public ArchiveReaderPoolDelegator(ArchiveReaderPool archiveReaderPool, ArchiveReader archiveReader) {
		this.archiveReaderPool = archiveReaderPool;
		this.archiveReader = archiveReader;
	}

	@Override
	public void openArchive(TypeableFile inputFile) throws Exception {
		archivePath = inputFile.getPath();
		
		ArchiveReader archiveReader = archiveReaderPool.removeArchiveReader(archivePath);
		if(archiveReader != null) {
			this.archiveReader = archiveReader;
		} else {
			this.archiveReader.openArchive(inputFile);
		}
	}

	@Override
	public void closeArchive() throws Exception {
		archiveReaderPool.addArchiveReader(archivePath, archiveReader);
	}

	@Override
	public TypeableFile readFile(Integer index) throws Exception {
		return archiveReader.readFile(index);
	}

	@Override
	public Integer readSize() throws Exception {
		return archiveReader.readSize();
	}

}
