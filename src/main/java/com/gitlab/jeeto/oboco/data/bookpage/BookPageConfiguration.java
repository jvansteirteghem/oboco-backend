package com.gitlab.jeeto.oboco.data.bookpage;

import java.util.ArrayList;
import java.util.List;

public class BookPageConfiguration {
	private Integer page;
	private List<ScaleConfiguration> scaleConfigurationList;
	public BookPageConfiguration() {
		super();
		scaleConfigurationList = new ArrayList<ScaleConfiguration>();
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public List<ScaleConfiguration> getScaleConfigurations() {
		return scaleConfigurationList;
	}
	public void setScaleConfigurations(List<ScaleConfiguration> scaleConfigurationList) {
		this.scaleConfigurationList = scaleConfigurationList;
	}
}
