package com.gitlab.jeeto.oboco.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// https://www.garykessler.net/library/file_sigs.html
// https://www.loc.gov/preservation/digital/formats/fdd/browse_list.shtml
public enum FileType {
	ZIP(".cbz", new int[] {0x50, 0x4B, 0x03, 0x04}),
	RAR(".cbr", new int[] {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00}),
	RAR5(".cbr", new int[] {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00}),
	SEVENZIP(".cb7", new int[] {0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C}),
	JPG(".jpg", new int[] {0xFF, 0xD8}),
	PNG(".png", new int[] {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
	
	private String extension;
	private int[] magic;
	
	private FileType(String extension, int[] magic) {
		this.extension = extension;
		this.magic = magic;
	}
	
	public int[] getMagic() {
		return magic;
	}
	
	public String getExtension() {
		return extension;
	}

	public static List<FileType> getListFileType() {
		List<FileType> listFileType = new ArrayList<FileType>();
		listFileType.add(FileType.ZIP);
		listFileType.add(FileType.RAR);
		listFileType.add(FileType.RAR5);
		listFileType.add(FileType.SEVENZIP);
		listFileType.add(FileType.JPG);
		listFileType.add(FileType.PNG);
		return listFileType;
	}
	
	public static FileType getFileType(File file) {
		try {
			InputStream inputStream = new FileInputStream(file);
			
			return getFileType(inputStream);
		} catch(Exception e) {
			return null;
		}
	}
	
	public static FileType getFileType(InputStream inputStream) {
		List<FileType> listFileType = getListFileType();
		
		return getFileType(inputStream, listFileType);
	}
	
	public static FileType getFileType(InputStream inputStream, List<FileType> listFileType) {
		try {
			FileType fileType = null;
			
			int i = 0;
			
			while(fileType == null && listFileType.size() != 0) {
				int data = inputStream.read();
				
				if(data == -1) {
					break;
				}
				
				Iterator<FileType> iterator = listFileType.iterator();
				while(iterator.hasNext()) {
					FileType nextFileType = iterator.next();
					
					int[] magic = nextFileType.getMagic();
					
					if(data == magic[i]) {
						if(i == magic.length - 1) {
							fileType = nextFileType;
							
							break;
						}
					} else {
						iterator.remove();
					}
				}
				
				i = i + 1;
			}
			
			return fileType;
		} catch(Exception e) {
			return null;
		} finally {
			try {
				if(inputStream != null) {
					inputStream.close();
				}
			} catch(Exception e) {
				// pass
			}
		}
	}
}
