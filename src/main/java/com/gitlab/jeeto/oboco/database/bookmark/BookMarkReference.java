package com.gitlab.jeeto.oboco.database.bookmark;

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

import com.gitlab.jeeto.oboco.database.book.Book;

@Entity
@Table(
	name = "bookMarkReferences",
	indexes = {
		@Index(name = "bookMarkReferenceBookIdBookMarkId", columnList = "bookId,bookMarkId", unique = true)
	}
)
public class BookMarkReference {
	private Long id;
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
