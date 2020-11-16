package com.gitlab.jeeto.oboco.common.archive;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.archive.impl.ArchiveReaderImpl;
import com.gitlab.jeeto.oboco.common.configuration.Configuration;
import com.gitlab.jeeto.oboco.common.configuration.ConfigurationManager;

public class ArchiveReaderFactory {
	private Configuration configuration;
	
	private Configuration getConfiguration() {
		if(configuration == null) {
			ConfigurationManager configurationManager = ConfigurationManager.getInstance();
			configuration = configurationManager.getConfiguration();
		}
		return configuration;
	}
	
	private ArchiveReaderPool archiveReaderPool;
	
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
			archiveReader = new ArchiveReaderImpl();
		} else if(FileType.RAR.equals(inputFileType)) {
			archiveReader = new ArchiveReaderImpl();
		} else if(FileType.RAR5.equals(inputFileType)) {
			archiveReader = new ArchiveReaderImpl();
		} else if(FileType.SEVENZIP.equals(inputFileType)) {
			archiveReader = new ArchiveReaderImpl();
		}
		
		if(archiveReader != null) {
			archiveReader = new ArchiveReaderPoolDelegator(archiveReaderPool, archiveReader);
		}
		
        return archiveReader;
	}

	public void start() {    
		Integer size = getConfiguration().getAsInteger("application.plugin.archive.archiveReaderPool.size", "25");
		Long interval = getConfiguration().getAsLong("application.plugin.archive.archiveReaderPool.interval", "60") * 1000L;
		Long age = getConfiguration().getAsLong("application.plugin.archive.archiveReaderPool.age", "600") * 1000L;
		
		archiveReaderPool = new ArchiveReaderPool(size, interval, age);
		archiveReaderPool.start();
	}

	public void stop() {  
		archiveReaderPool.stop();
	}
}
