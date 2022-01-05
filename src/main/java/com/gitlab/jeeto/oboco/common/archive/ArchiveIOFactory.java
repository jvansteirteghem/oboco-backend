package com.gitlab.jeeto.oboco.common.archive;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.FactoryManager;

public class ArchiveIOFactory extends Factory {
	private static ArchiveIOFactory instance;
	private SevenZipJBindingArchiveIOFactory sevenZipJBindingArchiveIOFactory;
	
	public static ArchiveIOFactory getInstance() {
		if(instance == null) {
			synchronized(ArchiveIOFactory.class) {
				if(instance == null) {
					instance = new ArchiveIOFactory();
					
					FactoryManager factoryManager = FactoryManager.getInstance();
					factoryManager.addFactory(instance);
				}
			}
		}
		return instance;
	}
	
	private ArchiveIOFactory() {
		super();
		
		sevenZipJBindingArchiveIOFactory = new SevenZipJBindingArchiveIOFactory();
	}
	
	public ArchiveReader getArchiveReader(ArchiveType archiveType) throws Exception {
        return sevenZipJBindingArchiveIOFactory.getArchiveReader(archiveType);
	}

	@Override
	public void start() {
		sevenZipJBindingArchiveIOFactory.start();
	}

	@Override
	public void stop() {
		sevenZipJBindingArchiveIOFactory.stop();
	}
}
