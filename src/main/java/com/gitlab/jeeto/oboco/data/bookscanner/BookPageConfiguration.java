package com.gitlab.jeeto.oboco.data.bookscanner;

import com.gitlab.jeeto.oboco.common.image.ScaleType;

public class BookPageConfiguration {
	private ScaleType scaleType;
	private Integer scaleWidth;
	private Integer scaleHeight;
	public BookPageConfiguration() {
		super();
	}
	public ScaleType getScaleType() {
		return scaleType;
	}
	public void setScaleType(ScaleType scaleType) {
		this.scaleType = scaleType;
	}
	public Integer getScaleWidth() {
		return scaleWidth;
	}
	public void setScaleWidth(Integer scaleWidth) {
		this.scaleWidth = scaleWidth;
	}
	public Integer getScaleHeight() {
		return scaleHeight;
	}
	public void setScaleHeight(Integer scaleHeight) {
		this.scaleHeight = scaleHeight;
	}
}
