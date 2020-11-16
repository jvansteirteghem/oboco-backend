package com.gitlab.jeeto.oboco.common.archive;

import java.util.Date;

public class ArchiveReaderPoolEntry {
	private Date date;
	private String archivePath;
	private ArchiveReader archiveReader;
	public ArchiveReaderPoolEntry(String archivePath, ArchiveReader archiveReader) {
		super();
		this.date = new Date();
		this.archivePath = archivePath;
		this.archiveReader = archiveReader;
	}
	public Date getDate() {
		return date;
	}
	public String getArchivePath() {
		return archivePath;
	}
	public ArchiveReader getArchiveReader() {
		return archiveReader;
	}
}
