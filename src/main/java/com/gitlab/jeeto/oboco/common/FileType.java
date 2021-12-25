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
	ZIP(new int[] {0x50, 0x4B, 0x03, 0x04}),
	RAR(new int[] {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00}),
	RAR5(new int[] {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00}),
	SEVENZIP(new int[] {0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C}),
	JPG(new int[] {0xFF, 0xD8}),
	PNG(new int[] {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
	
	private int[] magic;
	
	private FileType(int[] magic) {
		this.magic = magic;
	}
	
	public int[] getMagic() {
		return magic;
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
		try {
			List<FileType> fileTypeList = new ArrayList<FileType>();
			fileTypeList.add(FileType.ZIP);
			fileTypeList.add(FileType.RAR);
			fileTypeList.add(FileType.RAR5);
			fileTypeList.add(FileType.SEVENZIP);
			fileTypeList.add(FileType.JPG);
			fileTypeList.add(FileType.PNG);
			
			FileType fileType = null;
			
			int i = 0;
			
			while(fileType == null && fileTypeList.size() != 0) {
				int data = inputStream.read();
				
				if(data == -1) {
					break;
				}
				
				Iterator<FileType> iterator = fileTypeList.iterator();
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
