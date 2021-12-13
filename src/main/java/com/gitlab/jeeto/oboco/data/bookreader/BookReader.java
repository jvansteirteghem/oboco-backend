package com.gitlab.jeeto.oboco.data.bookreader;

import com.gitlab.jeeto.oboco.common.TypeableFile;

public interface BookReader {
	public void openBook(TypeableFile inputFile) throws Exception;
	public void closeBook() throws Exception;
    public TypeableFile getBookPage(Integer index) throws Exception;
    public Integer getNumberOfBookPages() throws Exception;
}
