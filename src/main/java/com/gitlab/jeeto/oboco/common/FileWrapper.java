package com.gitlab.jeeto.oboco.common;

public class FileWrapper<T> {
	private T file;
	private FileType fileType;
	
	public FileWrapper() {
		super();
	}
	
	public FileWrapper(T file, FileType fileType) {
		super();
		this.file = file;
		this.fileType = fileType;
	}
	
	public T getFile() {
		return file;
	}
	
	public void setFile(T file) {
		this.file = file;
	}
	
	public FileType getFileType() {
		return fileType;
	}
	
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
}
