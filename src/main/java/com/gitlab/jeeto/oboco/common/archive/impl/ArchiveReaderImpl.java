package com.gitlab.jeeto.oboco.common.archive.impl;

import java.io.File;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.FileWrapper;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReader;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class ArchiveReaderImpl implements ArchiveReader {
	private static Logger logger = LoggerFactory.getLogger(ArchiveReaderImpl.class.getName());
	
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
	private List<FileWrapper<ISimpleInArchiveItem>> listSimpleInArchiveItemWrapper = new ArrayList<FileWrapper<ISimpleInArchiveItem>>();
	
	@Override
	public void openArchive(FileWrapper<File> inputFileWrapper) throws Exception {
		List<FileType> listOutputFileType = new ArrayList<FileType>();
		listOutputFileType.add(FileType.JPG);
		listOutputFileType.add(FileType.PNG);
		
		File inputFile = inputFileWrapper.getFile();
		
		randomAccessFileIn = new RandomAccessFile(inputFile, "r");
		
		RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFileIn);
		
		IInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
		ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
		
		// start readListFile
		Instant durationStart = Instant.now();
		
		listSimpleInArchiveItemWrapper = new ArrayList<FileWrapper<ISimpleInArchiveItem>>();
        for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
            if (!simpleInArchiveItem.isFolder()) {
            	FileTypeOutStream fileTypeOutStream = new FileTypeOutStream();
            	try {
            		simpleInArchiveItem.extractSlow(fileTypeOutStream);
            	} catch(Exception e) {
            		// pass
            	}
        		FileType outputFileType = fileTypeOutStream.getFileType();
        		if(listOutputFileType.contains(outputFileType)) {
        			FileWrapper<ISimpleInArchiveItem> simpleInArchiveItemWrapper = new FileWrapper<ISimpleInArchiveItem>(simpleInArchiveItem, outputFileType);
        			
        			listSimpleInArchiveItemWrapper.add(simpleInArchiveItemWrapper);
        		}
            }
        }
        
        Instant durationStop = Instant.now();
        
        long duration = Duration.between(durationStart, durationStop).toMillis();
        
        logger.debug("readListFile: " + duration + " ms");
        // stop readListFile
        
        listSimpleInArchiveItemWrapper.sort(new NaturalOrderComparator<FileWrapper<ISimpleInArchiveItem>>() {
        	@Override
    		public String toString(FileWrapper<ISimpleInArchiveItem> o) {
        		try {
					return o.getFile().getPath();
				} catch (SevenZipException e) {
					return "";
				}
        	}
		});
	}

	@Override
	public void closeArchive() throws Exception {
		if(randomAccessFileIn != null) {
			randomAccessFileIn.close();
		}
	}

	@Override
	public FileWrapper<File> readFile(Integer index) throws Exception {
		RandomAccessFile randomAccessFileOut = null;
		try {
			FileWrapper<ISimpleInArchiveItem> simpleInArchiveItemFileTypeWrapper = listSimpleInArchiveItemWrapper.get(index);
			
			FileType outputFileType = simpleInArchiveItemFileTypeWrapper.getFileType();
			File outputFile = File.createTempFile("oboco-plugin-archive-sevenzipjbinding-", ".tmp");
			randomAccessFileOut = new RandomAccessFile(outputFile, "rw");
			
			RandomAccessFileOutStream randomAccessFileOutStream = new RandomAccessFileOutStream(randomAccessFileOut);
			
			ISimpleInArchiveItem simpleInArchiveItem = simpleInArchiveItemFileTypeWrapper.getFile();
			
			// start readFile
			Instant durationStart = Instant.now();
			
        	ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(randomAccessFileOutStream);
        	
        	Instant durationStop = Instant.now();
	        
	        long duration = Duration.between(durationStart, durationStop).toMillis();
	        
	        logger.debug("readFile: " + duration + " ms");
	        // stop readFile
            
            if (extractOperationResult != ExtractOperationResult.OK) {
                throw new Exception("extractOperationResult != ExtractOperationResult.OK");
            }
            
            FileWrapper<File> outputFileWrapper = new FileWrapper<File>(outputFile, outputFileType);
		    
		    return outputFileWrapper;
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
	public Integer readSize() throws Exception {
		return listSimpleInArchiveItemWrapper.size();
	}
}