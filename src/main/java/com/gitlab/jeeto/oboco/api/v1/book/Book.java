package com.gitlab.jeeto.oboco.api.v1.book;

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

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkReference;

@Entity
@Table(
	name = "books",
	indexes = {
		@Index(name = "bookFileId", columnList = "fileId", unique = false),
		@Index(name = "bookFilePath", columnList = "filePath", unique = true),
		@Index(name = "bookNormalizedName", columnList = "normalizedName", unique = false)
	}
)
public class Book {
	private Long id;
	private String fileId;
	private String filePath;
	private Date updateDate;
	private String name;
	private String normalizedName;
	private Integer numberOfPages;
	private BookCollection bookCollection;
	private List<BookMarkReference> bookMarkReferences;
	public Book() {
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
	@Column(name = "fileId", length = 64, nullable = false)
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	@Column(name = "filePath", length = 4096, nullable = false)
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
	@Column(name = "numberOfPages", nullable = false)
	public Integer getNumberOfPages() {
		return numberOfPages;
	}
	public void setNumberOfPages(Integer numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookCollectionId", referencedColumnName = "id")
	public BookCollection getBookCollection() {
		return bookCollection;
	}
	public void setBookCollection(BookCollection bookCollection) {
		this.bookCollection = bookCollection;
	}
	@OneToMany(mappedBy = "book", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	public List<BookMarkReference> getBookMarkReferences() {
		return bookMarkReferences;
	}
	public void setBookMarkReferences(List<BookMarkReference> bookMarkReferences) {
		this.bookMarkReferences = bookMarkReferences;
	}
}
