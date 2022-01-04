package com.gitlab.jeeto.oboco.data.book;

import java.util.Date;

public class BookReaderPoolEntry {
	private Date date;
	private String bookPath;
	private BookReader bookReader;
	public BookReaderPoolEntry(String bookPath, BookReader bookReader) {
		super();
		this.date = new Date();
		this.bookPath = bookPath;
		this.bookReader = bookReader;
	}
	public Date getDate() {
		return date;
	}
	public String getBookPath() {
		return bookPath;
	}
	public BookReader getBookReader() {
		return bookReader;
	}
}
