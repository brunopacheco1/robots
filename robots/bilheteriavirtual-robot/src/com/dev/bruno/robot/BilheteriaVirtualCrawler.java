package com.dev.bruno.robot;

import java.net.URLEncoder;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class BilheteriaVirtualCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("a")) {
			String link = a.attr("href").replaceAll("https", "http");
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			String title = a.parent().select("p.title").text();
			String local = a.parent().select("p.desc").text();
			String date = a.parent().select("p.date").text();
			String hour = a.parent().select("div.desc").text();
			
			String data = "title=" + URLEncoder.encode(title, "UTF-8") + "&local=" + URLEncoder.encode(local, "UTF-8") + "&date=" + URLEncoder.encode(date, "UTF-8") + "&hour=" + URLEncoder.encode(hour, "UTF-8"); 
			
			addCapturedLink(link + "?" + data);
		}
	}
}