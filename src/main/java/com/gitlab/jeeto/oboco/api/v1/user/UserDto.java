package com.gitlab.jeeto.oboco.api.v1.user;

import java.util.Date;
import java.util.List;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionDto;

@Schema(name = "User", description = "A user.")
@XmlRootElement(name = "User")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
	private Long id;
	private String name;
	private String password;
	private List<String> roles;
	private Date updateDate;
	private BookCollectionDto rootBookCollection;
	public UserDto() {
		super();
	}
	@Schema(name = "id")
	@XmlElement(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Schema(name = "name")
	@XmlElement(name = "name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Schema(name = "password")
	@XmlElement(name = "password")
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Schema(name = "roles")
	@XmlElement(name = "roles")
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	@JsonbDateFormat(JsonbDateFormat.TIME_IN_MILLIS)
	@Schema(name = "updateDate")
	@XmlElement(name = "updateDate")
	public Date getUpdateDate() {
		return updateDate;
	}
	@JsonbDateFormat(JsonbDateFormat.TIME_IN_MILLIS)
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@Schema(name = "rootBookCollection")
	@XmlElement(name = "rootBookCollection")
	public BookCollectionDto getRootBookCollection() {
		return rootBookCollection;
	}
	public void setRootBookCollection(BookCollectionDto rootBookCollection) {
		this.rootBookCollection = rootBookCollection;
	}
}
