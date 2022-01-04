package com.gitlab.jeeto.oboco.common.archive;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.archive.SevenZipJBindingArchiveReader.SevenZipJBindingRar5ArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.SevenZipJBindingArchiveReader.SevenZipJBindingRarArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.SevenZipJBindingArchiveReader.SevenZipJBindingSevenZipArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.SevenZipJBindingArchiveReader.SevenZipJBindingZipArchiveReader;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

public class SevenZipJBindingArchiveIOFactory implements Factory {
	private static Logger logger = LoggerFactory.getLogger(SevenZipJBindingArchiveReader.class.getName());
	private static boolean factoryStarted = false;
	
	public SevenZipJBindingArchiveIOFactory() {
		super();
	}
	
	public ArchiveReader getArchiveReader(ArchiveType archiveType) throws Exception {
		ArchiveReader archiveReader = null;
		
		if(ArchiveType.ZIP.equals(archiveType)) {
			archiveReader = new SevenZipJBindingZipArchiveReader();
		} else if(ArchiveType.RAR.equals(archiveType)) {
			archiveReader = new SevenZipJBindingRarArchiveReader();
		} else if(ArchiveType.RAR5.equals(archiveType)) {
			archiveReader = new SevenZipJBindingRar5ArchiveReader();
		} else if(ArchiveType.SEVENZIP.equals(archiveType)) {
			archiveReader = new SevenZipJBindingSevenZipArchiveReader();
		} else {
			throw new Exception("archiveType not supported.");
		}
		
        return archiveReader;
	}
	
	@Override
	public void start() {
		try {
			if(factoryStarted == false) {
				factoryStarted = true;
				
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
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		
	}
}
