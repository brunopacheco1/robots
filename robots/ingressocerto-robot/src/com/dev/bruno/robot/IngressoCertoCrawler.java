package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class IngressoCertoCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("a")) {
			if(a == null || a.attr("href") == null || a.attr("href").isEmpty()) {
				continue;
			}
			
			String link = "http://www.ingressocerto.com" + a.attr("href");
			
			if(link.matches(crawlerDTO.getDocumentURLRegex())) {
				addCapturedLink(link);
			}
			
			if(link.matches(crawlerDTO.getSourceURLRegex())) {
				link = link.split("\\?")[0] + ".partial?" + link.split("\\?")[1];

				addUrlsToCrawling(link);
			}
		}
	}
}