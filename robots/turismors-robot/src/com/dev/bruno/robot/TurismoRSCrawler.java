package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class TurismoRSCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("article.cConteudoEventoListaItem a")) {
			String link = "http://www.turismo.rs.gov.br" + a.attr("href");
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			link = "http://www.turismo.rs.gov.br/evento/" + link.split("evento\\/")[1].split("\\/")[0];
			
			addCapturedLink(link);
		}
		
		if(!body.select("article.cConteudoEventoListaItem a").isEmpty()) {
			for(Element a : body.select("div.cPaginadorButtons a")) {
				String link = "http://www.turismo.rs.gov.br" + a.attr("href");
				
				if(!link.matches(crawlerDTO.getSourceURLRegex())) {
					continue;
				}
				
				addUrlsToCrawling(link);
			}
		}
	}
}