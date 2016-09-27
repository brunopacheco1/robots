package com.dev.bruno.robot;

import java.util.HashSet;
import java.util.Set;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class GuicheWebCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		Set<String> toExclude = new HashSet<>();
		toExclude.add("/bondinho");
		toExclude.add("/cinemanguinhos");
		
		for(Element div : body.select("div.CaixaEvento")) {
			String link = div.attr("onclick").split("'")[1];
			
			if(link == null || link.isEmpty() || toExclude.contains(link)) {
				continue;
			}
			
			if(!link.startsWith("/")) {
				link = "/" + link;
			}
			
			link = "http://www.guicheweb.com.br" + link;
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			addCapturedLink(link);
		}
	}
}