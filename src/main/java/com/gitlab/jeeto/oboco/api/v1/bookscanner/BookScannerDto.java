package com.gitlab.jeeto.oboco.api.v1.bookscanner;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(name = "BookScanner", description = "A bookScanner")
@XmlRootElement(name="BookScanner")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookScannerDto {
	private String id;
	private String mode;
	private String status;
	public BookScannerDto() {
		super();
	}
	@Schema(name = "id")
	@XmlElement(name = "id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Schema(name = "mode")
	@XmlElement(name = "mode")
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	@Schema(name = "status")
	@XmlElement(name = "status")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
