package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.Date;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDto;

@Schema(name = "BookCollectionMark", description = "A bookCollectionMark.")
@XmlRootElement(name = "BookCollectionMark")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookCollectionMarkDto {
	private Long id;
	private Date createDate;
	private Date updateDate;
	private Integer numberOfBookPages;
	private Integer bookPage;
	private BookCollectionDto bookCollection;
	public BookCollectionMarkDto() {
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
	@Schema(name = "numberOfBookPages")
	@XmlElement(name = "numberOfBookPages")
	public Integer getNumberOfBookPages() {
		return numberOfBookPages;
	}
	public void setNumberOfBookPages(Integer numberOfBookPages) {
		this.numberOfBookPages = numberOfBookPages;
	}
	@Schema(name = "bookPage")
	@XmlElement(name = "bookPage")
	public Integer getBookPage() {
		return bookPage;
	}
	public void setBookPage(Integer bookPage) {
		this.bookPage = bookPage;
	}
	@Schema(name = "bookCollection")
	@XmlElement(name = "bookCollection")
	public BookCollectionDto getBookCollection() {
		return bookCollection;
	}
	public void setBookCollection(BookCollectionDto bookCollection) {
		this.bookCollection = bookCollection;
	}
}
