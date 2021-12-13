package com.gitlab.jeeto.oboco.api;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name="Linkable")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkableDto<T> {
	private T element;
	private T previousElement;
	private T nextElement;
	public LinkableDto() {
		super();
	}
	public T getElement() {
		return element;
	}
	public void setElement(T element) {
		this.element = element;
	}
	public T getPreviousElement() {
		return previousElement;
	}
	public void setPreviousElement(T previousElement) {
		this.previousElement = previousElement;
	}
	public T getNextElement() {
		return nextElement;
	}
	public void setNextElement(T nextElement) {
		this.nextElement = nextElement;
	}
}
