package com.dev.bruno.dto;

import java.util.ArrayList;
import java.util.List;

public class NormalizerDTO extends RobotDTO {

	private static final long serialVersionUID = 5419553535327077745L;
	
	private List<String> urlsToNormalizing = new ArrayList<>();

	private DocumentType documentType;
	
	public List<String> getUrlsToNormalizing() {
		return urlsToNormalizing;
	}

	public void setUrlsToNormalizing(List<String> urlsToNormalizing) {
		this.urlsToNormalizing = urlsToNormalizing;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}
}