package com.dev.bruno.robot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;

import com.google.gson.Gson;

@Crawling(documentType=DocumentType.SHOW)
public class G1Crawler extends Crawler {
	
	@SuppressWarnings("unchecked")
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Gson gson = new Gson();
		
		Map<String, Object> data = (Map<String, Object>) gson.fromJson(documentService.getDocumentAsString(url, crawlerDTO.getConnectionTimeout()), HashMap.class).get("shows");
		
		for(Object showsObj : data.values()) {
			List<Object> shows = (List<Object>) showsObj;
			
			for(Object showObj : shows) {
				Map<String, Object> show = (Map<String, Object>) showObj;
				
				String link = (String) show.get("full_url");
				
				if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
					continue;
				}
				
				addCapturedLink(link.trim());
			}
		}
	}
}