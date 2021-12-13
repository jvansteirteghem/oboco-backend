package com.gitlab.jeeto.oboco.database;

public class Linkable<T> {
	private T element;
	private T previousElement;
	private T nextElement;
	public Linkable() {
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
