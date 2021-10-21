package com.gitlab.jeeto.oboco.api.v1.bookmark;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.user.User;

@Entity
@Table(
	name = "bookCollectionMarks",
	indexes = {
		@Index(name = "bookCollectionMarkUserIdBookCollectionId", columnList = "userId,bookCollectionId", unique = true),
		@Index(name = "bookCollectionMarkCreateDate", columnList = "createDate", unique = false),
		@Index(name = "bookCollectionMarkUpdateDate", columnList = "updateDate", unique = false)
	}
)
public class BookCollectionMark {
	private Long id;
	private User user;
	private BookCollection bookCollection;
	private Date createDate;
	private Date updateDate;
	private Integer numberOfBookPages;
	private Integer bookPage;
	public BookCollectionMark() {
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookCollectionId", referencedColumnName = "id")
	public BookCollection getBookCollection() {
		return bookCollection;
	}
	public void setBookCollection(BookCollection bookCollection) {
		this.bookCollection = bookCollection;
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
	@Column(name = "numberOfBookPages", nullable = false)
	public Integer getNumberOfBookPages() {
		return numberOfBookPages;
	}
	public void setNumberOfBookPages(Integer numberOfBookPages) {
		this.numberOfBookPages = numberOfBookPages;
	}
	@Column(name = "bookPage", nullable = false)
	public Integer getBookPage() {
		return bookPage;
	}
	public void setBookPage(Integer bookPage) {
		this.bookPage = bookPage;
	}
}
