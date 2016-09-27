package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dev.bruno.robot.Crawler;
import com.dev.bruno.robot.Crawling;

@Crawling(documentType=DocumentType.SHOW)
public class AgitouGuarulhosCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element div : body.select("div#resultPaginaAgenda a")) {
			String link = div.attr("href").replaceAll("\\.\\.\\/\\.\\.\\/\\.\\.\\/", "/");
			
			link = "http://www.agitouberlandia.com.br" + link;
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			addCapturedLink(link);
		}
	}
}