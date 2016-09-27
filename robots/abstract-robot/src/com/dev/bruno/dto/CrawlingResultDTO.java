package com.dev.bruno.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CrawlingResultDTO implements Serializable {

	private static final long serialVersionUID = -1759826538751295929L;
	
	private List<String> capturedLinks = new ArrayList<>();

	public List<String> getCapturedLinks() {
		return capturedLinks;
	}

	public void setCapturedLinks(List<String> capturedLinks) {
		this.capturedLinks = capturedLinks;
	}
}