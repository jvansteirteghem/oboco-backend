package com.gitlab.jeeto.oboco.common.archive.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.gitlab.jeeto.oboco.common.FileType;

import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZipException;

public class FileTypeOutStream implements ISequentialOutStream {
	private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	private List<FileType> listFileType = FileType.getListFileType();
	private FileType fileType = null;
	
	public FileType getFileType() {
		return fileType;
	}
	
	@Override
	public int write(byte[] data) throws SevenZipException {
		try {
			byteArrayOutputStream.write(data);
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			
			fileType = FileType.getFileType(byteArrayInputStream, listFileType);
			
			if(fileType == null && listFileType.size() != 0) {
				return data.length;
			} else {
				throw new SevenZipException();
			}
		} catch(Exception e) {
			throw new SevenZipException(e);
		}
	}
}
