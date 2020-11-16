package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(
	name = "bookMarks",
	indexes = {
		@Index(name = "bookMarkUserName", columnList = "userName", unique = false),
		@Index(name = "bookMarkFileId", columnList = "fileId", unique = false)
	}
)
public class BookMark {
	private Long id;
	private String userName;
	private String fileId;
	private Date updateDate;
	private Integer page;
	private List<BookMarkReference> bookMarkReferences;
	public BookMark() {
		super();
	}
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name = "userName", length = 255, nullable = false)
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@Column(name = "fileId", length = 64, nullable = false)
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updateDate", nullable = false)
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@Column(name = "page", nullable = false)
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	@OneToMany(mappedBy = "bookMark", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<BookMarkReference> getBookMarkReferences() {
		return bookMarkReferences;
	}
	public void setBookMarkReferences(List<BookMarkReference> bookMarkReferences) {
		this.bookMarkReferences = bookMarkReferences;
	}
}
