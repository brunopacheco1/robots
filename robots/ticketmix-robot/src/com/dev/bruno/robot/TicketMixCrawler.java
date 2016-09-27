package com.dev.bruno.robot;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

@Crawling(documentType=DocumentType.SHOW)
public class TicketMixCrawler extends Crawler {
	
	@SuppressWarnings("unchecked")
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		String json = body.html().split("preencherListagem\\(")[1].split("\\);")[0];
		
		if(json == null || json.isEmpty()) {
			return;
		}
		
		Gson gson = new Gson();
		
		Map<String, Object> resultadoMap = gson.fromJson(json, HashMap.class);
		
		if(!resultadoMap.containsKey("entidades")) {
			return;
		}
		
		List<Map<String, Object>> lista = (List<Map<String, Object>>) resultadoMap.get("entidades");
		
		for(Map<String, Object> child : lista) {
			String link = (String) child.get("url");
			
			String date = URLEncoder.encode((String) child.get("horarios"), "UTF-8");
			
			String title = URLEncoder.encode((String) child.get("titulo"), "UTF-8");
			
			String location = URLEncoder.encode((String) child.get("local"), "UTF-8");
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			addCapturedLink(link + "?title=" + title + "&date=" + date + "&location=" + location);
		}
	}
}