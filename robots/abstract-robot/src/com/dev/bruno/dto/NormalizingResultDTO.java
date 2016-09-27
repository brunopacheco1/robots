package com.dev.bruno.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NormalizingResultDTO<T> implements Serializable {

private static final long serialVersionUID = -289144448358376459L;
	
	private List<T> capturedDocuments = new ArrayList<>();
	
	private List<String> problemLinks = new ArrayList<>();

	public List<T> getCapturedDocuments() {
		return capturedDocuments;
	}

	public void setCapturedDocuments(List<T> documents) {
		this.capturedDocuments = documents;
	}

	public List<String> getProblemLinks() {
		return problemLinks;
	}

	public void setProblemLinks(List<String> problemLinks) {
		this.problemLinks = problemLinks;
	}
}