package com.gitlab.jeeto.oboco.database;

import java.util.List;

public class PageableList<T> {
	private List<T> elements;
	private Long numberOfElements;
	private Integer page;
	private Integer pageSize;
	private Integer firstPage;
	private Integer lastPage;
	private Integer previousPage;
	private Integer nextPage;
	public PageableList(List<T> elements) {
		super();
		this.elements = elements;
		this.numberOfElements = new Long(elements.size());
	}
	public PageableList(List<T> elements, Long numberOfElements, Integer page, Integer pageSize) {
		super();
		this.elements = elements;
		this.numberOfElements = numberOfElements;
		this.page = page;
		this.pageSize = pageSize;
		if(this.numberOfElements > 0) {
			this.firstPage = 1;
			this.lastPage = new Long(this.numberOfElements / this.pageSize + ((this.numberOfElements % this.pageSize == 0L) ? 0L : 1L)).intValue();
			if(this.page > this.firstPage) {
				this.previousPage = this.page - 1;
				
				if(this.previousPage < this.firstPage) {
					this.previousPage = this.firstPage;
				} else if(this.previousPage > this.lastPage) {
					this.previousPage = this.lastPage;
				}
			}
			if(this.page < this.lastPage) {
				this.nextPage = this.page + 1;
				
				if(this.nextPage > this.lastPage) {
					this.nextPage = this.lastPage;
				} else if(this.nextPage < this.firstPage) {
					this.nextPage = this.firstPage;
				}
			}
		}
	}
	public List<T> getElements() {
		return elements;
	}
	public Long getNumberOfElements() {
		return numberOfElements;
	}
	public Integer getPage() {
		return page;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public Integer getFirstPage() {
		return firstPage;
	}
	public Integer getLastPage() {
		return lastPage;
	}
	public Integer getPreviousPage() {
		return previousPage;
	}
	public Integer getNextPage() {
		return nextPage;
	}
}
