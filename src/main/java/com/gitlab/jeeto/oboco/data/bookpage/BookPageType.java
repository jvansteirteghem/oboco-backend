package com.gitlab.jeeto.oboco.data.bookpage;

import java.io.File;
import java.io.IOException;

import com.gitlab.jeeto.oboco.common.FileHelper;

public enum BookPageType {
	JPEG(new String[] {".jpg", ".jpeg"}),
	PNG(new String[] {".png"});
	
	public static BookPageType getBookPageType(File bookPageFile) throws IOException {
		String bookPageFileExtension = FileHelper.getExtension(bookPageFile);
		
		if(bookPageFileExtension != null) {
			for(BookPageType bookPageType: BookPageType.values()) {
				for(String bookPageTypeFileExtension: bookPageType.getFileExtensions()) {
					if(bookPageFileExtension.equalsIgnoreCase(bookPageTypeFileExtension)) {
						return bookPageType;
					}
				}
			}
		}
		
		return null;
	}
	
	private String[] fileExtensions;
	
	private BookPageType(String[] fileExtensions) {
		this.fileExtensions = fileExtensions;
	}
	
	public String getFileExtension() {
		return fileExtensions[0];
	}

	public String[] getFileExtensions() {
		return fileExtensions;
	}
}
