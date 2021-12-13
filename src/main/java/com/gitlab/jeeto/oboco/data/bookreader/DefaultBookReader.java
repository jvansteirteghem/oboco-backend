package com.gitlab.jeeto.oboco.data.bookreader;

import java.util.ArrayList;
import java.util.List;

import com.gitlab.jeeto.oboco.data.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.FileType.Type;
import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.archive.ArchiveEntry;
import com.gitlab.jeeto.oboco.common.archive.ArchiveEntryType;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReaderFactory;

public class DefaultBookReader implements BookReader {
	private ArchiveReader archiveReader = null;
	private List<ArchiveEntry> archiveEntryList = null;
	
	@Override
	public void openBook(TypeableFile inputFile) throws Exception {
		if(archiveReader != null) {
			throw new Exception("book is open.");
		}
		
		ArchiveReaderFactory archiveReaderFactory = ArchiveReaderFactory.getInstance();
		archiveReader = archiveReaderFactory.getArchiveReader(inputFile.getFileType());
		archiveReader.openArchive(inputFile);
		
		List<FileType> outputFileTypeList = FileType.getFileTypeList(Type.IMAGE);
		
		archiveEntryList = new ArrayList<ArchiveEntry>();
		
		for(ArchiveEntry archiveEntry: archiveReader.getArchiveEntrySet()) {
			if(ArchiveEntryType.FILE.equals(archiveEntry.getType())) {
				FileType outputFileType = FileType.getFileType(archiveEntry.getName());
				
				if(outputFileTypeList.contains(outputFileType)) {
					archiveEntryList.add(archiveEntry);
				}
			}
		}
		
		archiveEntryList.sort(new NaturalOrderComparator<ArchiveEntry>() {
			@Override
			public String toString(ArchiveEntry o) {
				return o.getName();
			}
		});
	}

	@Override
	public void closeBook() throws Exception {
		if(archiveReader == null) {
			throw new Exception("book is closed.");
		}
		
		archiveReader.closeArchive();
	}

	@Override
	public TypeableFile getBookPage(Integer index) throws Exception {
		if(archiveReader == null) {
			throw new Exception("book is closed.");
		}
		
		ArchiveEntry archiveEntry = archiveEntryList.get(index);
		
		return archiveReader.getFile(archiveEntry);
	}

	@Override
	public Integer getNumberOfBookPages() throws Exception {
		if(archiveReader == null) {
			throw new Exception("book is closed.");
		}
		
		return archiveEntryList.size();
	}

}
