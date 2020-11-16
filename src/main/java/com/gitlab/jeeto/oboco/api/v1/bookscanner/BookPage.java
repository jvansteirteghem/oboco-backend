package com.gitlab.jeeto.oboco.api.v1.bookscanner;

import java.util.ArrayList;
import java.util.List;

public class BookPage {
	private Integer page;
	private List<BookPageConfiguration> bookPageConfigurationList;
	public BookPage() {
		super();
		bookPageConfigurationList = new ArrayList<BookPageConfiguration>();
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public List<BookPageConfiguration> getBookPageConfigurationList() {
		return bookPageConfigurationList;
	}
	public void setBookPageConfigurationList(List<BookPageConfiguration> bookPageConfigurationList) {
		this.bookPageConfigurationList = bookPageConfigurationList;
	}
}
