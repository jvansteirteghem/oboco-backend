package com.gitlab.jeeto.oboco.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class TypeableFile extends File {
	private static final long serialVersionUID = 1L;
	private boolean hasType = false;
	private FileType type = null;
	private boolean hasExtension = false;
	private String extension = null;

	public TypeableFile(File parent, String child) {
		super(parent, child);
	}

	public TypeableFile(String parent, String child) {
		super(parent, child);
	}

	public TypeableFile(String pathname) {
		super(pathname);
	}

	public TypeableFile(URI uri) {
		super(uri);
	}
	
	public TypeableFile(File file) {
		super(file, "");
	}
	
	public TypeableFile(File file, FileType type) {
		super(file, "");
		
		this.hasType = true;
		this.type = type;
	}
	
	public FileType getType() {
		if(hasType == false) {
			hasType = true;
			
			type = FileType.getFileType(this);
		}
		return type;
	}
	
	public String getExtension() {
		if(hasExtension == false) {
			hasExtension = true;
			
			String fileName = this.getName();
			
			int index = fileName.lastIndexOf('.');
			if(index != -1) {
				extension = fileName.substring(index);
				extension = extension.toLowerCase();
			}
		}
		return extension;
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public String getParent() {
		return super.getParent();
	}

	@Override
	public File getParentFile() {
		return super.getParentFile();
	}
	
	public TypeableFile getParentTypeableFile() {
		File parentFile = super.getParentFile();
		TypeableFile parentTypeableFile = new TypeableFile(parentFile);
		return parentTypeableFile;
	}

	@Override
	public String getPath() {
		return super.getPath();
	}

	@Override
	public boolean isAbsolute() {
		return super.isAbsolute();
	}

	@Override
	public String getAbsolutePath() {
		return super.getAbsolutePath();
	}

	@Override
	public File getAbsoluteFile() {
		return super.getAbsoluteFile();
	}
	
	public TypeableFile getAbsoluteTypeableFile() {
		return map(super.getAbsoluteFile());
	}

	@Override
	public String getCanonicalPath() throws IOException {
		return super.getCanonicalPath();
	}

	@Override
	public File getCanonicalFile() throws IOException {
		return super.getCanonicalFile();
	}

	@Deprecated
	@Override
	public URL toURL() throws MalformedURLException {
		return super.toURL();
	}

	@Override
	public URI toURI() {
		return super.toURI();
	}

	@Override
	public boolean canRead() {
		return super.canRead();
	}

	@Override
	public boolean canWrite() {
		return super.canWrite();
	}

	@Override
	public boolean exists() {
		return super.exists();
	}

	@Override
	public boolean isDirectory() {
		return super.isDirectory();
	}

	@Override
	public boolean isFile() {
		return super.isFile();
	}

	@Override
	public boolean isHidden() {
		return super.isHidden();
	}

	@Override
	public long lastModified() {
		return super.lastModified();
	}

	@Override
	public long length() {
		return super.length();
	}

	@Override
	public boolean createNewFile() throws IOException {
		return super.createNewFile();
	}

	@Override
	public boolean delete() {
		return super.delete();
	}

	@Override
	public void deleteOnExit() {
		super.deleteOnExit();
	}

	@Override
	public String[] list() {
		return super.list();
	}

	@Override
	public String[] list(FilenameFilter filter) {
		return super.list(filter);
	}

	@Override
	public File[] listFiles() {
		return super.listFiles();
	}
	
	public TypeableFile[] listTypeableFiles() {
		return map(super.listFiles());
	}

	@Override
	public File[] listFiles(FilenameFilter filter) {
		return super.listFiles(filter);
	}
	
	public TypeableFile[] listTypeableFiles(FileFilter filter) {
		return map(super.listFiles(filter));
	}

	@Override
	public File[] listFiles(FileFilter filter) {
		return super.listFiles(filter);
	}

	@Override
	public boolean mkdir() {
		return super.mkdir();
	}

	@Override
	public boolean mkdirs() {
		return super.mkdirs();
	}

	@Override
	public boolean renameTo(File dest) {
		return super.renameTo(dest);
	}

	@Override
	public boolean setLastModified(long time) {
		return super.setLastModified(time);
	}

	@Override
	public boolean setReadOnly() {
		return super.setReadOnly();
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		return super.setWritable(writable, ownerOnly);
	}

	@Override
	public boolean setWritable(boolean writable) {
		return super.setWritable(writable);
	}

	@Override
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		return super.setReadable(readable, ownerOnly);
	}

	@Override
	public boolean setReadable(boolean readable) {
		return super.setReadable(readable);
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		return super.setExecutable(executable, ownerOnly);
	}

	@Override
	public boolean setExecutable(boolean executable) {
		return super.setExecutable(executable);
	}

	@Override
	public boolean canExecute() {
		return super.canExecute();
	}

	@Override
	public long getTotalSpace() {
		return super.getTotalSpace();
	}

	@Override
	public long getFreeSpace() {
		return super.getFreeSpace();
	}

	@Override
	public long getUsableSpace() {
		return super.getUsableSpace();
	}

	@Override
	public int compareTo(File pathname) {
		return super.compareTo(pathname);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public Path toPath() {
		return super.toPath();
	}
	
	private TypeableFile map(File file) {
		TypeableFile typeableFile = new TypeableFile(file);
		return typeableFile;
	}
	
	private TypeableFile[] map(File[] files) {
		TypeableFile[] typeableFiles = null;
		if(files != null) {
			typeableFiles = new TypeableFile[files.length];
			for(int i = 0; i < files.length; i = i + 1) {
				File file = files[i];
				TypeableFile typeableFile = null;
				if(file != null) {
					typeableFile = map(file);
				}
				typeableFiles[i] = typeableFile;
			}
		}
		return typeableFiles;
	}
}
