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
			logger.error("Error", e);
		}
	}
	
	private RandomAccessFile randomAccessFileIn = null;
	private Map<ArchiveEntry, ISimpleInArchiveItem> simpleInArchiveItemMap = null;
	
	@Override
	public void openArchive(TypeableFile inputFile) throws Exception {
		if(randomAccessFileIn != null) {
			throw new Exception("archive is open.");
		}
		
		randomAccessFileIn = new RandomAccessFile(inputFile, "r");
		
		RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFileIn);
		
		IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
		ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
		
		simpleInArchiveItemMap = new HashMap<ArchiveEntry, ISimpleInArchiveItem>();
		
		for(ISimpleInArchiveItem simpleInArchiveItem: simpleInArchive.getArchiveItems()) {
			String name = simpleInArchiveItem.getPath();
			
			ArchiveEntryType type;
			if(simpleInArchiveItem.isFolder()) {
				type = ArchiveEntryType.DIRECTORY;
			} else {
				type = ArchiveEntryType.FILE;
			}
			
			ArchiveEntry archiveEntry = new ArchiveEntry(name, type);
			
			simpleInArchiveItemMap.put(archiveEntry, simpleInArchiveItem);
		}
	}

	@Override
	public void closeArchive() throws Exception {
		if(randomAccessFileIn == null) {
			throw new Exception("archive is closed.");
		}
		
		randomAccessFileIn.close();
	}

	@Override
	public TypeableFile getFile(ArchiveEntry archiveEntry) throws Exception {
		if(randomAccessFileIn == null) {
			throw new Exception("archive is closed.");
		}
		
		RandomAccessFile randomAccessFileOut = null;
		try {
			ISimpleInArchiveItem simpleInArchiveItem = simpleInArchiveItemMap.get(archiveEntry);
			
			TypeableFile outputFile = new TypeableFile(File.createTempFile("oboco-plugin-archive-sevenzipjbinding-", ".tmp"));
			randomAccessFileOut = new RandomAccessFile(outputFile, "rw");
			
			RandomAccessFileOutStream randomAccessFileOutStream = new RandomAccessFileOutStream(randomAccessFileOut);
			
			ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(randomAccessFileOutStream);
			
			if (extractOperationResult != ExtractOperationResult.OK) {
				throw new Exception("extractOperationResult != ExtractOperationResult.OK");
			}
			
			return outputFile;
		} catch(Exception e) {
			throw e;
		} finally {
			try {
				if(randomAccessFileOut != null) {
					randomAccessFileOut.close();
				}
			} catch(Exception e) {
				// pass
			}
		}
	}

	@Override
	public Set<ArchiveEntry> getArchiveEntrySet() throws Exception {
		if(randomAccessFileIn == null) {
			throw new Exception("archive is closed.");
		}
		
		return simpleInArchiveItemMap.keySet();
	}
}