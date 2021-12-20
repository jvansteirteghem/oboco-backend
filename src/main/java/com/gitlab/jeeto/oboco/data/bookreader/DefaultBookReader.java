package com.gitlab.jeeto.oboco.data.bookreader;

import java.util.ArrayList;
import java.util.List;

import com.gitlab.jeeto.oboco.data.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.FileType.Type;
import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReaderEntry;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReaderFactory;

public class DefaultBookReader implements BookReader {
	private Boolean archiveReaderOpen = false;
	private ArchiveReader archiveReader = null;
	private List<ArchiveReaderEntry> archiveReaderEntryList = null;
	
	@Override
	public void openBook(TypeableFile inputFile) throws Exception {
		if(archiveReaderOpen) {
			throw new Exception("book open.");
		}
		
		try {
			ArchiveReaderFactory archiveReaderFactory = ArchiveReaderFactory.getInstance();
			archiveReader = archiveReaderFactory.getArchiveReader(inputFile.getFileType());
			archiveReader.openArchive(inputFile);
			
			List<FileType> outputFileTypeList = FileType.getFileTypeList(Type.IMAGE);
			
			archiveReaderEntryList = new ArrayList<ArchiveReaderEntry>();
			
			for(ArchiveReaderEntry archiveReaderEntry: archiveReader.getArchiveReaderEntrySet()) {
				if(ArchiveReaderEntry.Type.FILE.equals(archiveReaderEntry.getType())) {
					FileType outputFileType = FileType.getFileType(archiveReaderEntry.getName());
					
					if(outputFileTypeList.contains(outputFileType)) {
						archiveReaderEntryList.add(archiveReaderEntry);
					}
				}
			}
			
			archiveReaderEntryList.sort(new NaturalOrderComparator<ArchiveReaderEntry>() {
				@Override
				public String toString(ArchiveReaderEntry o) {
					return o.getName();
				}
			});
			
			archiveReaderOpen = true;
		} finally {
			if(archiveReaderOpen == false) {
				archiveReaderEntryList = null;
				
				try {
					if(archiveReader != null) {
						archiveReader.closeArchive();
						archiveReader = null;
					}
				} catch(Exception e) {
					// pass
				}
			}
		}
	}

	@Override
	public void closeBook() throws Exception {
		if(archiveReaderOpen == false) {
			throw new Exception("book not open.");
		}
		
		archiveReaderEntryList = null;
		
		try {
			if(archiveReader != null) {
				archiveReader.closeArchive();
				archiveReader = null;
			}
		} catch(Exception e) {
			// pass
		}
		
		archiveReaderOpen = false;
	}

	@Override
	public TypeableFile getBookPage(Integer index) throws Exception {
		if(archiveReaderOpen == false) {
			throw new Exception("book not open.");
		}
		
		ArchiveReaderEntry archiveReaderEntry = archiveReaderEntryList.get(index);
		
		return archiveReader.getFile(archiveReaderEntry);
	}

	@Override
	public Integer getNumberOfBookPages() throws Exception {
		if(archiveReaderOpen == false) {
			throw new Exception("book not open.");
		}
		
		return archiveReaderEntryList.size();
	}

}
