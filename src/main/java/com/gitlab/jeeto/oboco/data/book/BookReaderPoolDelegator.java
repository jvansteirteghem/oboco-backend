package com.gitlab.jeeto.oboco.data.book;

import java.io.File;

public class BookReaderPoolDelegator implements BookReader {
	private BookType bookType;
	private BookReaderPool bookReaderPool;
	private BookReader bookReader;
	private String bookPath;
	
	public BookReaderPoolDelegator(BookType bookType, BookReaderPool bookReaderPool) {
		super();
		
		this.bookType = bookType;
		this.bookReaderPool = bookReaderPool;
		this.bookReader = null;
	}

	@Override
	public void openBook(File inputFile) throws Exception {
		bookPath = inputFile.getPath();
		
		BookReader bookReader = bookReaderPool.removeBookReader(bookPath);
		if(bookReader == null) {
			BookReaderFactory bookReaderFactory = BookReaderFactory.getInstance();
			
			bookReader = bookReaderFactory.getBookReader(bookType);
			bookReader.openBook(inputFile);
		}
		
		this.bookReader = bookReader;
	}

	@Override
	public void closeBook() throws Exception {
		if(bookReader != null) {
			bookReaderPool.addBookReader(bookPath, bookReader);
		}
	}
	
	@Override
	public BookType getBookType() {
		return bookReader.getBookType();
	}

	@Override
	public File getBookPage(Integer index) throws Exception {
		return bookReader.getBookPage(index);
	}

	@Override
	public Integer getNumberOfBookPages() throws Exception {
		return bookReader.getNumberOfBookPages();
	}

}
