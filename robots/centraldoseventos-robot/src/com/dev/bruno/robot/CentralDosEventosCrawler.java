package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class CentralDosEventosCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("a")) {
			String link = a.attr("href");
			
			if(link.matches(crawlerDTO.getDocumentURLRegex())) {
				addCapturedLink(link);
			} else if(link.matches(crawlerDTO.getSourceURLRegex())) {
				addUrlsToCrawling(link);
			}
		}
	}
}