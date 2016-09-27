package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class Ticket360Crawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("a")) {
			String link = "https://www.ticket360.com.br" + a.attr("href");
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			if(link.matches(crawlerDTO.getDocumentURLRegex())) {
				addCapturedLink(link);
			}
			
			if(link.matches(crawlerDTO.getSourceURLRegex())) {
				CrawlerDTO newSource = new CrawlerDTO();
				
				newSource.setDepth(crawlerDTO.getDepth() + 1);
				newSource.setEndDepth(crawlerDTO.getEndDepth());
				newSource.setConnectionTimeout(crawlerDTO.getConnectionTimeout());
				newSource.setSourceURLRegex(crawlerDTO.getSourceURLRegex());
				newSource.setDocumentURLRegex(crawlerDTO.getDocumentURLRegex());
				
				addUrlsToCrawling(link);
			}
		}
	}
}