package com.dev.bruno.robot;

import java.util.ArrayList;
import java.util.List;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class TicketsForFunCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		if(!body.select("img.tryAgainButton").isEmpty()) {
			logger.info("TRÁFEGO INTENSO NO SITE[" + url + "]!");
			return;
		}
		
		List<String> toExclude = new ArrayList<>();
		toExclude.add("OFERTAS");
		toExclude.add("pdv");
		toExclude.add("COMERCIAL");
		toExclude.add("atend");
		toExclude.add("ATEND");
		toExclude.add("eticket");
		toExclude.add("COMOCOMPRA");
		toExclude.add("grupos");
		
		for(Element a : body.select("a")) {
			String link = "http://premier.ticketsforfun.com.br" + a.attr("href");
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			boolean found = false;
			for (String end : toExclude) {
				if(link.endsWith(end)) {
					found = true;
					
					break;
				}
			}
			
			if(found) {
				continue;
			}
			
			addCapturedLink(link);
		}
	}
}