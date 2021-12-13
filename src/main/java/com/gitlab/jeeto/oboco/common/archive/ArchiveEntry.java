package com.gitlab.jeeto.oboco.common.archive;

public class ArchiveEntry {
	private String name;
	private ArchiveEntryType type;
	public ArchiveEntry(String name, ArchiveEntryType type) {
		super();
		this.name = name;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public ArchiveEntryType getType() {
		return type;
	}
}
