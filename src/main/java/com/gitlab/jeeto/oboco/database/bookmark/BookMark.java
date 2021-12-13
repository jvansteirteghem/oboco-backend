package com.gitlab.jeeto.oboco.database.bookmark;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.gitlab.jeeto.oboco.database.user.User;

@Entity
@Table(
	name = "bookMarks",
	indexes = {
		@Index(name = "bookMarkUserIdFileId", columnList = "userId,fileId", unique = true),
		@Index(name = "bookMarkFileId", columnList = "fileId", unique = false),
		@Index(name = "bookMarkCreateDate", columnList = "createDate", unique = false),
		@Index(name = "bookMarkUpdateDate", columnList = "updateDate", unique = false)
	}
)
public class BookMark {
	private Long id;
	private User user;
	private String fileId;
	private Date createDate;
	private Date updateDate;
	private Integer numberOfPages;
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", referencedColumnName = "id", nullable = false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	@Column(name = "fileId", length = 64, nullable = false)
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createDate", nullable = false)
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updateDate", nullable = false)
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@Column(name = "numberOfPages", nullable = false)
	public Integer getNumberOfPages() {
		return numberOfPages;
	}
	public void setNumberOfPages(Integer numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
	@Column(name = "page", nullable = false)
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	@OneToMany(mappedBy = "bookMark", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	public List<BookMarkReference> getBookMarkReferences() {
		return bookMarkReferences;
	}
	public void setBookMarkReferences(List<BookMarkReference> bookMarkReferences) {
		this.bookMarkReferences = bookMarkReferences;
	}
}
