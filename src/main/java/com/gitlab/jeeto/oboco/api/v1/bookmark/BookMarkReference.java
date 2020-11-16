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

@Entity
@Table(
	name = "bookMarkReferences",
	indexes = {
		@Index(name = "bookMarkReferenceUserName", columnList = "userName", unique = false),
		@Index(name = "bookMarkReferenceUserNameBookIdBookMarkId", columnList = "userName,bookId,bookMarkId", unique = true),
		@Index(name = "bookMarkReferenceFileId", columnList = "fileId", unique = false)
	}
)
public class BookMarkReference {
	private Long id;
	private String userName;
	private String fileId;
	private Date updateDate;
	private Book book;
	private BookMark bookMark;
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
}
