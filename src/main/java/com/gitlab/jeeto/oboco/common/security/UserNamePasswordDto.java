package com.gitlab.jeeto.oboco.common.security;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(name = "UserNamePassword", description = "A userNamePassword.")
@XmlRootElement(name = "UserNamePassword")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserNamePasswordDto {
	private String name;
	private String password;
	public UserNamePasswordDto() {
		super();
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
}
