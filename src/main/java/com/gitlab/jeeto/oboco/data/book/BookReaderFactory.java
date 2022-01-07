package com.gitlab.jeeto.oboco.data.book;

public class BookReaderFactory {
	private static BookReaderFactory instance;
	
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
	}
	
	public BookReader getBookReader(BookType bookType) throws Exception {
		BookReader bookReader = null;
		
		if(BookType.CBZ.equals(bookType) || BookType.CBR.equals(bookType) || BookType.CB7.equals(bookType)) {
			bookReader = new DefaultBookReader(bookType);
		} else {
			throw new Exception("bookType not supported.");
		}
		
		return bookReader;
	}
}
