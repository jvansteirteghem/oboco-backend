package com.gitlab.jeeto.oboco.data.book;

import java.io.File;
import java.io.IOException;

import com.gitlab.jeeto.oboco.common.FileHelper;

public enum BookType {
	CBZ(new String[] {".cbz", ".zip"}),
	CBR(new String[] {".cbr", ".rar"}),
	CB7(new String[] {".cb7", ".7z"});
	
	public static BookType getBookType(File bookFile) throws IOException {
		String bookFileExtension = FileHelper.getExtension(bookFile);
		
		if(bookFileExtension != null) {
			for(BookType bookType: BookType.values()) {
				for(String bookTypeFileExtension: bookType.getFileExtensions()) {
					if(bookFileExtension.equalsIgnoreCase(bookTypeFileExtension)) {
						return bookType;
					}
				}
			}
		}
		
		return null;
	}
	
	private String[] fileExtensions;
	
	private BookType(String[] fileExtensions) {
		this.fileExtensions = fileExtensions;
	}
	
	public String getFileExtension() {
		return fileExtensions[0];
	}
	
	public String[] getFileExtensions() {
		return fileExtensions;
	}
}
