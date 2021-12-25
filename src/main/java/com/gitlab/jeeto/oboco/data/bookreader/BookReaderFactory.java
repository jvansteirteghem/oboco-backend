package com.gitlab.jeeto.oboco.data.bookreader;

import java.util.ArrayList;
import java.util.List;

public class BookReaderFactory {
	private static BookReaderFactory instance;
	private List<String> inputFileExtensionList;
	
	public static BookReaderFactory getInstance() {
		if(instance == null) {
			synchronized(BookReaderFactory.class) {
				if(instance == null) {
					instance = new BookReaderFactory();
				}
			}
		}
		return instance;
	}
	
	private BookReaderFactory() {
		super();
		
		inputFileExtensionList = new ArrayList<String>();
		inputFileExtensionList.add(".cbz");
		inputFileExtensionList.add(".zip");
		inputFileExtensionList.add(".cbr");
		inputFileExtensionList.add(".rar");
		inputFileExtensionList.add(".cb7");
		inputFileExtensionList.add(".7z");
	}
	
	public BookReader getBookReader(String inputFileExtension) throws Exception {
		BookReader bookReader = null;
		
		if(inputFileExtensionList.contains(inputFileExtension)) {
			bookReader = new DefaultBookReader();
		} else {
			throw new Exception("fileExtension not supported.");
		}
		
		return bookReader;
	}
}
