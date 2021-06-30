package com.gitlab.jeeto.oboco.api.v1.book;

import java.util.Date;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkDto;

@Schema(name = "Book", description = "A book.")
@XmlRootElement(name = "Book")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookDto {
	private Long id;
	private Date createDate;
	private Date updateDate;
	private String name;
	private Integer numberOfPages;
	private BookCollectionDto bookCollection;
	private BookMarkDto bookMark;
	public BookDto() {
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
	@Schema(name = "createDate")
	@XmlElement(name = "createDate")
	public Date getCreateDate() {
		return createDate;
	}
	@JsonbDateFormat(JsonbDateFormat.TIME_IN_MILLIS)
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	@JsonbDateFormat(JsonbDateFormat.TIME_IN_MILLIS)
	@Schema(name = "updateDate")
	@XmlElement(name = "updateDate")
	public Date getUpdateDate() {
		return updateDate;
	}
	@JsonbDateFormat(JsonbDateFormat.TIME_IN_MILLIS)
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@Schema(name = "name")
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Schema(name = "numberOfPages")
	@XmlElement(name = "numberOfPages")
	public Integer getNumberOfPages() {
		return numberOfPages;
	}
	public void setNumberOfPages(Integer numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
	@Schema(name = "bookCollection")
	@XmlElement(name = "bookCollection")
	public BookCollectionDto getBookCollection() {
		return bookCollection;
	}
	public void setBookCollection(BookCollectionDto bookCollection) {
		this.bookCollection = bookCollection;
	}
	@Schema(name = "bookMark")
	@XmlElement(name = "bookMark")
	public BookMarkDto getBookMark() {
		return bookMark;
	}
	public void setBookMark(BookMarkDto bookMark) {
		this.bookMark = bookMark;
	}
}
