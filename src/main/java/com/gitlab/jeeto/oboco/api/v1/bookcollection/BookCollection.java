package com.gitlab.jeeto.oboco.api.v1.bookcollection;

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

import com.gitlab.jeeto.oboco.api.v1.book.Book;

@Entity
@Table(
	name = "bookCollections",
	indexes = {
		@Index(name = "bookCollectionDirectoryPath", columnList = "directoryPath", unique = true),
		@Index(name = "bookCollectionNormalizedName", columnList = "normalizedName", unique = false)
	}
)
public class BookCollection {
	private Long id;
	private String directoryPath;
	private Date updateDate;
	private String name;
	private String normalizedName;
	private BookCollection parentBookCollection;
	private List<BookCollection> bookCollections;
	private Integer numberOfBookCollections;
	private List<Book> books;
	private Integer numberOfBooks;
	private Integer number;
	public BookCollection() {
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
	@Column(name = "directoryPath", length = 4096, nullable = false)
	public String getDirectoryPath() {
		return directoryPath;
	}
	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updateDate", nullable = false)
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@Column(name = "name", length = 255, nullable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name = "normalizedName", length = 255, nullable = false)
	public String getNormalizedName() {
		return normalizedName;
	}
	public void setNormalizedName(String normalizedName) {
		this.normalizedName = normalizedName;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentBookCollectionId", referencedColumnName = "id")
	public BookCollection getParentBookCollection() {
		return parentBookCollection;
	}
	public void setParentBookCollection(BookCollection parentBookCollection) {
		this.parentBookCollection = parentBookCollection;
	}
	@OneToMany(mappedBy = "parentBookCollection", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	public List<BookCollection> getBookCollections() {
		return bookCollections;
	}
	public void setBookCollections(List<BookCollection> bookCollections) {
		this.bookCollections = bookCollections;
	}
	@Column(name = "numberOfBookCollections", nullable = false)
	public Integer getNumberOfBookCollections() {
		return numberOfBookCollections;
	}
	public void setNumberOfBookCollections(Integer numberOfBookCollections) {
		this.numberOfBookCollections = numberOfBookCollections;
	}
	@OneToMany(mappedBy = "bookCollection", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	public List<Book> getBooks() {
		return books;
	}
	public void setBooks(List<Book> books) {
		this.books = books;
	}
	@Column(name = "numberOfBooks", nullable = false)
	public Integer getNumberOfBooks() {
		return numberOfBooks;
	}
	public void setNumberOfBooks(Integer numberOfBooks) {
		this.numberOfBooks = numberOfBooks;
	}
	@Column(name = "number", nullable = false)
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
}
