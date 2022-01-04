package com.gitlab.jeeto.oboco.common.archive;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class SevenZipJBindingArchiveReader implements ArchiveReader {
	private Boolean archiveOpened = false;
	private RandomAccessFile archiveInputFile = null;
	private IInArchive archive = null;
	private List<ArchiveReaderEntry> archiveReaderEntryList = null;
	
	public ArchiveFormat getArchiveFormat() {
		return null;
	}
	
	@Override
	public void openArchive(File inputFile) throws Exception {
		if(archiveOpened) {
			throw new Exception("archive opened.");
		}
		
		try {
			archiveInputFile = new RandomAccessFile(inputFile, "r");
			
			archive = SevenZip.openInArchive(getArchiveFormat(), new RandomAccessFileInStream(archiveInputFile));
			
			ISimpleInArchive s = archive.getSimpleInterface();
			
			archiveReaderEntryList = new ArrayList<ArchiveReaderEntry>();
			
			for(ISimpleInArchiveItem archiveEntry: s.getArchiveItems()) {
				ArchiveReaderEntry archiveReaderEntry = new SevenZipJBindingArchiveReaderEntry(archiveEntry);
				
				archiveReaderEntryList.add(archiveReaderEntry);
			}
			
			archiveOpened = true;
		} finally {
			if(archiveOpened == false) {
				archiveReaderEntryList = null;
				
				try {
					if(archive != null) {
						archive.close();
						archive = null;
					}
				} catch(Exception e) {
					// pass
				}
				
				try {
					if(archiveInputFile != null) {
						archiveInputFile.close();
						archiveInputFile = null;
					}
				} catch(Exception e) {
					// pass
				}
			}
		}
	}

	@Override
	public void closeArchive() throws Exception {
		if(archiveOpened == false) {
			throw new Exception("archive not opened.");
		}
		
		archiveReaderEntryList = null;
		
		try {
			if(archive != null) {
				archive.close();
				archive = null;
			}
		} catch(Exception e) {
			// pass
		}
		
		try {
			if(archiveInputFile != null) {
				archiveInputFile.close();
				archiveInputFile = null;
			}
		} catch(Exception e) {
			// pass
		}
		
		archiveOpened = false;
	}

	@Override
	public void read(ArchiveReaderEntry archiveReaderEntry, File outputFile) throws Exception {
		if(archiveOpened == false) {
			throw new Exception("archive not opened.");
		}
		
		RandomAccessFile archiveOutputFile = null;
		try {
			ISimpleInArchiveItem archiveEntry = ((SevenZipJBindingArchiveReaderEntry) archiveReaderEntry).getArchiveEntry();
			
			archiveOutputFile = new RandomAccessFile(outputFile, "rw");
			
			ExtractOperationResult result = archiveEntry.extractSlow(new RandomAccessFileOutStream(archiveOutputFile));
			
			if(result != ExtractOperationResult.OK) {
				throw new Exception("archiveEntry not OK.");
			}
		} finally {
			try {
				if(archiveOutputFile != null) {
					archiveOutputFile.close();
				}
			} catch(Exception e) {
				// pass
			}
		}
	}

	@Override
	public List<ArchiveReaderEntry> getArchiveReaderEntries() throws Exception {
		if(archiveOpened == false) {
			throw new Exception("archive not opened.");
		}
		
		return archiveReaderEntryList;
	}
	
	public static class SevenZipJBindingZipArchiveReader extends SevenZipJBindingArchiveReader implements ZipArchiveReader {
		public ArchiveFormat getArchiveFormat() {
			return ArchiveFormat.ZIP;
		}
	}
	
	public static class SevenZipJBindingRarArchiveReader extends SevenZipJBindingArchiveReader implements RarArchiveReader {
		public ArchiveFormat getArchiveFormat() {
			return ArchiveFormat.RAR;
		}
	}
	
	public static class SevenZipJBindingRar5ArchiveReader extends SevenZipJBindingArchiveReader implements Rar5ArchiveReader {
		public ArchiveFormat getArchiveFormat() {
			return ArchiveFormat.RAR5;
		}
	}
	
	public static class SevenZipJBindingSevenZipArchiveReader extends SevenZipJBindingArchiveReader implements SevenZipArchiveReader {
		public ArchiveFormat getArchiveFormat() {
			return ArchiveFormat.SEVEN_ZIP;
		}
	}
}
