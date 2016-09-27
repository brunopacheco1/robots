package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class IngressoRapidoCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("a.linkPaginacao")) {
			String link = "http://www.ingressorapido.com.br/" + a.attr("href");
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			addUrlsToCrawling(link);
		}
		
		for(Element a : body.select("a")) {
			String link = a.attr("href");
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			if(link.startsWith("Evento")) {
				link = "http://www.ingressorapido.com.br/" + a.attr("href");
			}
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			addCapturedLink(link);
		}
	}
}