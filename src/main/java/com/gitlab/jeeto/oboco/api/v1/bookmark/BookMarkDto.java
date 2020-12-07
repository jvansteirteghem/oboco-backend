package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.Date;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.api.v1.book.BookDto;

@Schema(name = "BookMark", description = "A bookMark.")
@XmlRootElement(name = "BookMark")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookMarkDto {
	private Long id;
	private Date updateDate;
	private Integer page;
	private BookDto book;
	public BookMarkDto() {
		super();
	}
	@Schema(name = "id")
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@JsonbDateFormat(JsonbDateFormat.TIME_IN_MILLIS)
	@Schema(name = "updateDate")
	@XmlElement(name = "updateDate")
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@Schema(name = "page")
	@XmlElement(name = "page")
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	@Schema(name = "book")
	@XmlElement(name = "book")
	public BookDto getBook() {
		return book;
	}
	public void setBook(BookDto book) {
		this.book = book;
	}
}
