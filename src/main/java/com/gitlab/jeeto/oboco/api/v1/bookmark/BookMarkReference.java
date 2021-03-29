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

import com.gitlab.jeeto.oboco.api.v1.book.Book;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.user.User;

@Entity
@Table(
	name = "bookMarkReferences",
	indexes = {
		@Index(name = "bookMarkReferenceUserIdBookIdBookMarkId", columnList = "userId,bookId,bookMarkId", unique = true),
		@Index(name = "bookMarkReferenceFileId", columnList = "fileId", unique = false)
	}
)
public class BookMarkReference {
	private Long id;
	private User user;
	private String fileId;
	private Date updateDate;
	private Book book;
	private BookMark bookMark;
	private BookCollection rootBookCollection;
	public BookMarkReference() {
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
	@Column(name = "updateDate", nullable = false)
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookId", referencedColumnName = "id")
	public Book getBook() {
		return book;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookMarkId", referencedColumnName = "id", nullable = false)
	public BookMark getBookMark() {
		return bookMark;
	}
	public void setBookMark(BookMark bookMark) {
		this.bookMark = bookMark;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rootBookCollectionId", referencedColumnName = "id", nullable = false)
	public BookCollection getRootBookCollection() {
		return rootBookCollection;
	}
	public void setRootBookCollection(BookCollection rootBookCollection) {
		this.rootBookCollection = rootBookCollection;
	}
}
