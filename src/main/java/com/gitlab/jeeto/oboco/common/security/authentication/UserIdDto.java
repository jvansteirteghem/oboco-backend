package com.gitlab.jeeto.oboco.common.security.authentication;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(name = "UserId", description = "A userId.")
@XmlRootElement(name = "UserId")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserIdDto {
	private String name;
	private List<String> roles;
	private String idToken;
	private String refreshToken;
	public UserIdDto() {
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
	@Schema(name = "roles")
	@XmlElement(name = "roles")
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	@Schema(name = "idToken")
	@XmlElement(name = "idToken")
	public String getIdToken() {
		return idToken;
	}
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
	@Schema(name = "refreshToken")
	@XmlElement(name = "refreshToken")
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
