package com.gitlab.jeeto.oboco.common.archive;

import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class SevenZipJBindingArchiveReaderEntry implements ArchiveReaderEntry {
	private ISimpleInArchiveItem archiveEntry;
	
	public SevenZipJBindingArchiveReaderEntry(ISimpleInArchiveItem archiveEntry) {
		super();
		
		this.archiveEntry = archiveEntry;
	}
	
	public ISimpleInArchiveItem getArchiveEntry() {
		return archiveEntry;
	}
	
	@Override
	public String getName() {
		try {
			return archiveEntry.getPath();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isDirectory() {
		try {
			return archiveEntry.isFolder();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
