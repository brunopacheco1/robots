package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Crawling(documentType=DocumentType.SHOW)
public class FacebookCrawler extends Crawler {
	
	private String accessToken;
	
	public FacebookCrawler() {
		String credencialsUrl = "https://graph.facebook.com/oauth/access_token?client_id=1113523825333831&client_secret=03e95b72b98a50103bd20872e232efc9&grant_type=client_credentials";
		
    	Client client = ClientBuilder.newClient();
		
		accessToken = client.target(credencialsUrl).request().get(String.class);
		
		accessToken = accessToken.substring(accessToken.indexOf("=") + 1);
	}
	
	@SuppressWarnings("unchecked")
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		if(accessToken == null) {
			throw new Exception("Falha ao adquirir chave de acesso ao Facebook.");
		}
		
		String pageId = url.split("\\/")[url.split("\\/").length - 2];
		
		String facebookURL = "https://graph.facebook.com/v2.5/" + pageId + "?fields=id,name,events&access_token=" + accessToken;
		
		Gson gson = new GsonBuilder().create();
		
		Client client = ClientBuilder.newClient();
		
		Map<String, Object> response = gson.fromJson(client.target(facebookURL).request().get(String.class), HashMap.class);
		
		if(!response.containsKey("events")) {
			return;
		}
		
		Map<String, Object> events = (Map<String, Object>) response.get("events");
		
		List<Map<String, Object>> resultado = (List<Map<String, Object>>) events.get("data");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
		
		for(Map<String, Object> event : resultado) {
			String id = (String) event.get("id");
			
			String startTimeStr = (String) event.get("start_time");
			
			if(startTimeStr.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{4}$")) {
				startTimeStr = startTimeStr.substring(0, startTimeStr.lastIndexOf("-"));
			} else if(startTimeStr.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{4}$")) {
				startTimeStr = startTimeStr.substring(0, startTimeStr.lastIndexOf("+"));
			}
			
			Date startTime = dateFormat.parse(startTimeStr);
			
			String link = "https://www.facebook.com/events/" + id + "/";
			
			if(link.matches(crawlerDTO.getDocumentURLRegex()) && startTime.after(new Date())) {
				addCapturedLink(link);
			}
		}
	}
}