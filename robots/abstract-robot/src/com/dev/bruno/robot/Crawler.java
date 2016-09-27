package com.dev.bruno.robot;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.dev.bruno.dto.CrawlerDTO;

public abstract class Crawler {
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Set<String> capturedLinks = new HashSet<>();
	
	private Set<String> urlsToCrawling = new HashSet<>();
	
	protected WebDocumentService documentService = new WebDocumentService();
	
	public abstract void run(CrawlerDTO crawlerDTO, String url) throws Exception;
	
	protected void addCapturedLink(String link) {
		capturedLinks.add(link);
	}
	
	protected void addUrlsToCrawling(String link) {
		urlsToCrawling.add(link);
	}

	public Set<String> getCapturedLinks() {
		return capturedLinks;
	}

	public Set<String> getUrlsToCrawling() {
		return urlsToCrawling;
	}
}