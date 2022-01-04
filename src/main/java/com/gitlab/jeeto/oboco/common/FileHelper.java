package com.gitlab.jeeto.oboco.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class FileHelper {
	public static String getName(File file) {
		return getName(file.getPath());
	}
	
	public static String getName(String path) {
		String name = path;
		
		int index = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		if(index != -1) {
			name = name.substring(index + 1);
		}
		
		index = name.lastIndexOf('.');
		if(index != -1) {
			name = name.substring(0, index);
		}
		
		return name;
	}
	
	public static String getExtension(File file) {	
		return getExtension(file.getPath());
	}
	
	public static String getExtension(String path) {
		String extension = null;
		
		int index = path.lastIndexOf('.');
		if(index != -1) {
			extension = path.substring(index);
			extension = extension.toLowerCase();
		}
			
		return extension;
	}
	
	public static FileType getFileType(File file, List<FileType> fileTypeList) throws IOException {
		FileType fileType = null;
		
		InputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			
			int fileSignatureIndex = 0;
			
			while(fileType == null && fileTypeList.size() != 0) {
				int fileSignature = fileInputStream.read();
				
				if(fileSignature == -1) {
					break;
				}
				
				Iterator<FileType> iterator = fileTypeList.iterator();
				while(iterator.hasNext()) {
					FileType nextFileType = iterator.next();
					
					int[] nextFileTypeSignature = nextFileType.getSignature();
					
					if(fileSignature == nextFileTypeSignature[fileSignatureIndex]) {
						if(fileSignatureIndex == nextFileTypeSignature.length - 1) {
							fileType = nextFileType;
							
							break;
						}
					} else {
						iterator.remove();
					}
				}
				
				fileSignatureIndex = fileSignatureIndex + 1;
			}
		} finally {
			try {
				if(fileInputStream != null) {
					fileInputStream.close();
				}
			} catch(Exception e) {
				// pass
			}
		}
		
		return fileType;
	}
}
