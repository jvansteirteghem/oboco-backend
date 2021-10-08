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
		@Index(name = "bookMarkReferenceBookIdBookMarkId", columnList = "bookId,bookMarkId", unique = true),
		@Index(name = "bookMarkReferenceCreateDate", columnList = "createDate", unique = false),
		@Index(name = "bookMarkReferenceUpdateDate", columnList = "updateDate", unique = false)
	}
)
public class BookMarkReference {
	private Long id;
	private Book book;
	private BookMark bookMark;
	private Date createDate;
	private Date updateDate;
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
}
