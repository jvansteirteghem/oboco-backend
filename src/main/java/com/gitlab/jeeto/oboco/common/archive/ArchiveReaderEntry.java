package com.gitlab.jeeto.oboco.common.archive;

public class ArchiveReaderEntry {
	public enum Type {
		FILE,
		DIRECTORY;
	}
	
	private String name;
	private Type type;
	public ArchiveReaderEntry(String name, Type type) {
		super();
		this.name = name;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public Type getType() {
		return type;
	}
}
