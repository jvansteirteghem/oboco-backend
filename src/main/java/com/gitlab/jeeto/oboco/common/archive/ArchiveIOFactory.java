package com.gitlab.jeeto.oboco.common.archive;

import com.gitlab.jeeto.oboco.common.Factory;

public class ArchiveIOFactory implements Factory {
	private SevenZipJBindingArchiveIOFactory sevenZipJBindingArchiveIOFactory;
	
	public ArchiveIOFactory() {
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
