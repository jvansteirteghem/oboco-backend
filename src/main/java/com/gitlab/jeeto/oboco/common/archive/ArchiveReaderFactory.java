package com.gitlab.jeeto.oboco.common.archive;

import com.gitlab.jeeto.oboco.common.FileType;

public class ArchiveReaderFactory {
	private static ArchiveReaderFactory instance;
	
	public static ArchiveReaderFactory getInstance() {
		if(instance == null) {
			synchronized(ArchiveReaderFactory.class) {
				if(instance == null) {
					instance = new ArchiveReaderFactory();
				}
			}
		}
		return instance;
	}
	
	private ArchiveReaderFactory() {
		super();
	}
	
	public ArchiveReader getArchiveReader(FileType inputFileType) throws Exception {
		ArchiveReader archiveReader = null;
		
		if(FileType.ZIP.equals(inputFileType)) {
			archiveReader = new DefaultArchiveReader();
		} else if(FileType.RAR.equals(inputFileType)) {
			archiveReader = new DefaultArchiveReader();
		} else if(FileType.RAR5.equals(inputFileType)) {
			archiveReader = new DefaultArchiveReader();
		} else if(FileType.SEVENZIP.equals(inputFileType)) {
			archiveReader = new DefaultArchiveReader();
		} else {
			throw new Exception("fileType not supported.");
		}
		
        return archiveReader;
	}
}
