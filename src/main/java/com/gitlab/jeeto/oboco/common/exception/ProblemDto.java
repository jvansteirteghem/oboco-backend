package com.gitlab.jeeto.oboco.common.exception;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(name = "Problem", description = "A problem.")
@XmlRootElement(name = "Problem")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDto {
	private Integer statusCode;
	private String code;
	private String description;
	public ProblemDto() {
		super();
	}
	public ProblemDto(Integer statusCode, String code, String description) {
		super();
		this.statusCode = statusCode;
		this.code = code;
		this.description = description;
	}
	@Schema(name = "statusCode")
	@XmlElement(name = "statusCode")
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	@Schema(name = "code")
	@XmlElement(name = "code")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Schema(name = "description")
	@XmlElement(name = "description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
