package com.gitlab.jeeto.oboco.api.v1.user;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(name = "UserPassword", description = "A userPassword.")
@XmlRootElement(name = "UserPassword")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPasswordDto {
	private String password;
	private String updatePassword;
	public UserPasswordDto() {
		super();
	}
	@Schema(name = "password")
	@XmlElement(name = "password")
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Schema(name = "updatePassword")
	@XmlElement(name = "updatePassword")
	public String getUpdatePassword() {
		return updatePassword;
	}
	public void setUpdatePassword(String updatePassword) {
		this.updatePassword = updatePassword;
	}
}
