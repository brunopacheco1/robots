package com.dev.bruno.robot;

import java.net.URLEncoder;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class TudusCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("h2.product-name a")) {
			String link = a.attr("href");
			
			String title = a.attr("title");
			
			String local = a.parent().parent().select("div.product-category").text();
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			link += "?title=" + URLEncoder.encode(title,"UTF-8") + "&local=" +  URLEncoder.encode(local,"UTF-8");
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			addCapturedLink(link);
		}
	}
}