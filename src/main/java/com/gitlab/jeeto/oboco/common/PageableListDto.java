package com.gitlab.jeeto.oboco.common;

import java.util.List;

public class PageableListDto<T> {
	private List<T> elements;
	private Long numberOfElements;
	private Integer page;
	private Integer pageSize;
	private Integer firstPage;
	private Integer lastPage;
	private Integer previousPage;
	private Integer nextPage;
	public PageableListDto() {
		super();
	}
	public List<T> getElements() {
		return elements;
	}
	public void setElements(List<T> elements) {
		this.elements = elements;
	}
	public Long getNumberOfElements() {
		return numberOfElements;
	}
	public void setNumberOfElements(Long numberOfElements) {
		this.numberOfElements = numberOfElements;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getFirstPage() {
		return firstPage;
	}
	public void setFirstPage(Integer firstPage) {
		this.firstPage = firstPage;
	}
	public Integer getLastPage() {
		return lastPage;
	}
	public void setLastPage(Integer lastPage) {
		this.lastPage = lastPage;
	}
	public Integer getPreviousPage() {
		return previousPage;
	}
	public void setPreviousPage(Integer previousPage) {
		this.previousPage = previousPage;
	}
	public Integer getNextPage() {
		return nextPage;
	}
	public void setNextPage(Integer nextPage) {
		this.nextPage = nextPage;
	}
}
