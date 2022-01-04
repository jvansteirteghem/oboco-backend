package com.gitlab.jeeto.oboco.data.book;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.gitlab.jeeto.oboco.common.FactoryManager;
import com.gitlab.jeeto.oboco.common.FileHelper;
import com.gitlab.jeeto.oboco.data.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.data.bookpage.BookPageType;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReaderEntry;
import com.gitlab.jeeto.oboco.common.archive.ArchiveIOFactory;
import com.gitlab.jeeto.oboco.common.archive.ArchiveType;

public class DefaultBookReader implements BookReader {
	private BookType bookType;
	private Boolean archiveReaderOpened = false;
	private ArchiveReader archiveReader = null;
	private List<ArchiveReaderEntry> archiveReaderEntryList = null;
	
	public DefaultBookReader(BookType bookType) {
		super();
		
		this.bookType = bookType;
	}
	
	@Override
	public void openBook(File inputFile) throws Exception {
		if(archiveReaderOpened) {
			throw new Exception("book opened.");
		}
		
		try {
			FactoryManager factoryManager = FactoryManager.getInstance();
			
			ArchiveIOFactory archiveIOFactory = factoryManager.getFactory(ArchiveIOFactory.class);
			ArchiveType archiveType = ArchiveType.getArchiveType(inputFile);
			archiveReader = archiveIOFactory.getArchiveReader(archiveType);
			archiveReader.openArchive(inputFile);
			
			archiveReaderEntryList = new ArrayList<ArchiveReaderEntry>();
			
			for(ArchiveReaderEntry archiveReaderEntry: archiveReader.getArchiveReaderEntries()) {
				if(archiveReaderEntry.isDirectory() == false) {
					File outputFile = new File(archiveReaderEntry.getName());
					
					BookPageType bookPageType = BookPageType.getBookPageType(outputFile);
					
					if(bookPageType != null) {
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
			
			archiveReaderOpened = true;
		} finally {
			if(archiveReaderOpened == false) {
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
		if(archiveReaderOpened == false) {
			throw new Exception("book not opened.");
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
		
		archiveReaderOpened = false;
	}
	
	@Override
	public BookType getBookType() {
		return bookType;
	}

	@Override
	public File getBookPage(Integer index) throws Exception {
		if(archiveReaderOpened == false) {
			throw new Exception("book not opened.");
		}
		
		ArchiveReaderEntry archiveReaderEntry = archiveReaderEntryList.get(index);
		
		File outputFile = null;
		try {
			outputFile = File.createTempFile("oboco-", FileHelper.getExtension(archiveReaderEntry.getName()));
			
			archiveReader.read(archiveReaderEntry, outputFile);
		} catch(Exception e) {
			if(outputFile != null) {
				outputFile.delete();
			}
			
			throw e;
		}
		
		return outputFile;
	}

	@Override
	public Integer getNumberOfBookPages() throws Exception {
		if(archiveReaderOpened == false) {
			throw new Exception("book not opened.");
		}
		
		return archiveReaderEntryList.size();
	}

}
