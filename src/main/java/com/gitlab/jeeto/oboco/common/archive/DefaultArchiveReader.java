package com.gitlab.jeeto.oboco.common.archive;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.TypeableFile;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class DefaultArchiveReader implements ArchiveReader {
	private static Logger logger = LoggerFactory.getLogger(DefaultArchiveReader.class.getName());
	
	static {
		try {
			String name = System.getProperty("os.name").split(" ")[0].toLowerCase();
			String architecture = System.getProperty("os.arch").toLowerCase();
			File libraryFile = null;
			if(name.equals("windows")) {
				libraryFile = new File("lib-native/sevenzipjbinding/" + name + "/" + architecture + "/lib7-Zip-JBinding.dll");
			} else if(name.equals("mac")) {
				libraryFile = new File("lib-native/sevenzipjbinding/" + name + "/" + architecture + "/lib7-Zip-JBinding.dylib");
			} else {
				libraryFile = new File("lib-native/sevenzipjbinding/" + name + "/" + architecture + "/lib7-Zip-JBinding.so");
			}
			if(libraryFile.isFile()) {
				logger.debug("load library: " + libraryFile.getAbsolutePath());
				System.load(libraryFile.getAbsolutePath());
			} else {
				logger.debug("load library: 7-Zip-JBinding");
				System.loadLibrary("7-Zip-JBinding");
			}
			
			try {
				logger.debug("initialize loaded library");
				SevenZip.initLoadedLibraries();
			} catch(SevenZipNativeInitializationException e) {
				logger.debug("load library and initialize loaded library");
				SevenZip.initSevenZipFromPlatformJAR();
			}
		} catch(SevenZipNativeInitializationException e) {
			logger.error("error", e);
		}
	}
	
	private Boolean archiveOpen = false;
	private RandomAccessFile archiveInputFile = null;
	private IInArchive archive = null;
	private Map<ArchiveReaderEntry, ISimpleInArchiveItem> archiveEntryMap = null;
	
	@Override
	public void openArchive(TypeableFile inputFile) throws Exception {
		if(archiveOpen) {
			throw new Exception("archive open.");
		}
		
		try {
			archiveInputFile = new RandomAccessFile(inputFile, "r");
			
			archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(archiveInputFile));
			
			ISimpleInArchive s = archive.getSimpleInterface();
			
			archiveEntryMap = new HashMap<ArchiveReaderEntry, ISimpleInArchiveItem>();
			
			for(ISimpleInArchiveItem archiveEntry: s.getArchiveItems()) {
				String name = archiveEntry.getPath();
				
				ArchiveReaderEntry.Type type;
				if(archiveEntry.isFolder()) {
					type = ArchiveReaderEntry.Type.DIRECTORY;
				} else {
					type = ArchiveReaderEntry.Type.FILE;
				}
				
				ArchiveReaderEntry archiveReaderEntry = new ArchiveReaderEntry(name, type);
				
				archiveEntryMap.put(archiveReaderEntry, archiveEntry);
			}
			
			archiveOpen = true;
		} finally {
			if(archiveOpen == false) {
				archiveEntryMap = null;
				
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
		if(archiveOpen == false) {
			throw new Exception("archive not open.");
		}
		
		archiveEntryMap = null;
		
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
		
		archiveOpen = false;
	}

	@Override
	public TypeableFile getFile(ArchiveReaderEntry archiveReaderEntry) throws Exception {
		if(archiveOpen == false) {
			throw new Exception("archive not open.");
		}
		
		TypeableFile outputFile;
		
		RandomAccessFile archiveOutputFile = null;
		try {
			ISimpleInArchiveItem archiveEntry = archiveEntryMap.get(archiveReaderEntry);
			
			outputFile = new TypeableFile(File.createTempFile("oboco-plugin-archive-sevenzipjbinding-", ".tmp"));
			
			archiveOutputFile = new RandomAccessFile(outputFile, "rw");
			
			ExtractOperationResult result = archiveEntry.extractSlow(new RandomAccessFileOutStream(archiveOutputFile));
			
			if(result != ExtractOperationResult.OK) {
				throw new Exception("error: " + result);
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
		
		return outputFile;
	}

	@Override
	public Set<ArchiveReaderEntry> getArchiveReaderEntrySet() throws Exception {
		if(archiveOpen == false) {
			throw new Exception("archive not open.");
		}
		
		return archiveEntryMap.keySet();
	}
}