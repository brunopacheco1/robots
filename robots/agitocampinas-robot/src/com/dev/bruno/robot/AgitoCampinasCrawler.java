package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dev.bruno.robot.Crawler;
import com.dev.bruno.robot.Crawling;

@Crawling(documentType=DocumentType.SHOW)
public class AgitoCampinasCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element a : body.select("ul.guia-list a")) {
			String link = a.attr("href").replaceAll("\\.\\.\\/\\.\\.\\/\\.\\.", "http://www.agitocampinas.com.br");
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			addCapturedLink(link);
		}
	}
}