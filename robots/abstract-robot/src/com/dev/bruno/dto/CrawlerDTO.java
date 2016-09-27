package com.dev.bruno.dto;

import java.util.ArrayList;
import java.util.List;

public class CrawlerDTO extends RobotDTO {

	private static final long serialVersionUID = -1479634687315000297L;
	
	private String documentURLRegex;
	
	private String sourceURLRegex;
	
	private Long depth = 1l;
	
	private Long endDepth = 1l;
	
	private DocumentType documentType;
	
	private List<String> urlsToCrawling = new ArrayList<>();

	public String getDocumentURLRegex() {
		return documentURLRegex;
	}

	public void setDocumentURLRegex(String documentURLRegex) {
		this.documentURLRegex = documentURLRegex;
	}

	public String getSourceURLRegex() {
		return sourceURLRegex;
	}

	public void setSourceURLRegex(String sourceURLRegex) {
		this.sourceURLRegex = sourceURLRegex;
	}

	public Long getDepth() {
		return depth;
	}

	public void setDepth(Long depth) {
		this.depth = depth;
	}

	public Long getEndDepth() {
		return endDepth;
	}

	public void setEndDepth(Long endDepth) {
		this.endDepth = endDepth;
	}

	public List<String> getUrlsToCrawling() {
		return urlsToCrawling;
	}

	public void setUrlsToCrawling(List<String> urlsToCrawling) {
		this.urlsToCrawling = urlsToCrawling;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}
}