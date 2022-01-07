package com.gitlab.jeeto.oboco.data.book;

import java.io.File;

public interface BookReader {
	public void openBook(File inputFile) throws Exception;
	public void closeBook() throws Exception;
	public BookType getBookType();
    public File getBookPage(Integer index) throws Exception;
    public Integer getNumberOfBookPages() throws Exception;
}
