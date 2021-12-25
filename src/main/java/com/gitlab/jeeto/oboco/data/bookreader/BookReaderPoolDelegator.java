package com.gitlab.jeeto.oboco.data.bookreader;

import com.gitlab.jeeto.oboco.common.TypeableFile;

public class BookReaderPoolDelegator implements BookReader {
	private BookReaderPool bookReaderPool;
	private BookReader bookReader;
	private String bookPath;
	
	public BookReaderPoolDelegator(BookReaderPool bookReaderPool) {
		this.bookReaderPool = bookReaderPool;
		this.bookReader = null;
	}

	@Override
	public void openBook(TypeableFile inputFile) throws Exception {
		bookPath = inputFile.getPath();
		
		BookReader bookReader = bookReaderPool.removeBookReader(bookPath);
		if(bookReader == null) {
			BookReaderFactory bookReaderFactory = BookReaderFactory.getInstance();
			
			bookReader = bookReaderFactory.getBookReader(inputFile.getExtension());
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
	public TypeableFile getBookPage(Integer index) throws Exception {
		return bookReader.getBookPage(index);
	}

	@Override
	public Integer getNumberOfBookPages() throws Exception {
		return bookReader.getNumberOfBookPages();
	}

}
