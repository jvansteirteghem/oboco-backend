package com.gitlab.jeeto.oboco.api.v1.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
import javax.persistence.Transient;

import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMark;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkReference;

@Entity
@Table(
	name = "users",
	indexes = {
		@Index(name = "userName", columnList = "name", unique = true)
	}
)
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String name;
	private String password;
	private String passwordHash;
	private List<String> roles;
	private Date updateDate;
	private BookCollection rootBookCollection;
	private List<BookMarkReference> bookMarkReferences;
	private List<BookMark> bookMarks;
	public User() {
		super();
		roles = new ArrayList<String>();
	}
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name = "name", length = 255, nullable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Transient
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Column(name = "passwordHash", length = 60, nullable = false)
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "userRoles", joinColumns = @JoinColumn(name = "userId", referencedColumnName = "id"))
	@Column(name = "role", length = 255, nullable = false)
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updateDate", nullable = false)
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rootBookCollectionId", referencedColumnName = "id", nullable = true)
	public BookCollection getRootBookCollection() {
		return rootBookCollection;
	}
	public void setRootBookCollection(BookCollection rootBookCollection) {
		this.rootBookCollection = rootBookCollection;
	}
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<BookMarkReference> getBookMarkReferences() {
		return bookMarkReferences;
	}
	public void setBookMarkReferences(List<BookMarkReference> bookMarkReferences) {
		this.bookMarkReferences = bookMarkReferences;
	}
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<BookMark> getBookMarks() {
		return bookMarks;
	}
	public void setBookMarks(List<BookMark> bookMarks) {
		this.bookMarks = bookMarks;
	}
}
