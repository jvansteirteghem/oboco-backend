package com.gitlab.jeeto.oboco.problem;

public class Problem {
	private Integer statusCode;
	private String code;
	private String description;
	public Problem() {
		super();
	}
	public Problem(Integer statusCode, String code, String description) {
		super();
		this.statusCode = statusCode;
		this.code = code;
		this.description = description;
	}
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
