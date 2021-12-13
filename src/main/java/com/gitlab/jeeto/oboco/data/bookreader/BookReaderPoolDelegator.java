package com.gitlab.jeeto.oboco.data.bookreader;

import com.gitlab.jeeto.oboco.common.TypeableFile;

public class BookReaderPoolDelegator implements BookReader {
	private BookReaderPool bookReaderPool;
	private BookReader bookReader;
	private String bookPath;
	
	public BookReaderPoolDelegator(BookReaderPool bookReaderPool) {
		this.bookReaderPool = bookReaderPool;
	}

	@Override
	public void openBook(TypeableFile inputFile) throws Exception {
		bookPath = inputFile.getPath();
		
		BookReader bookReader = bookReaderPool.removeBookReader(bookPath);
		if(bookReader != null) {
			this.bookReader = bookReader;
		} else {
			this.bookReader = new DefaultBookReader();
			this.bookReader.openBook(inputFile);
		}
	}

	@Override
	public void closeBook() throws Exception {
		bookReaderPool.addBookReader(bookPath, bookReader);
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
