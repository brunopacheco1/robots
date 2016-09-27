package com.dev.bruno.robot;

import java.net.URLEncoder;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class ShowDeIngressosCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element div : body.select("div.content")) {
			String link = div.select("a").attr("href");
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			String title = URLEncoder.encode(div.select("a").text(), "UTF-8");
			
			String date = URLEncoder.encode(div.select("div.cost").text(), "UTF-8");
			
			String location = URLEncoder.encode(div.select("div.category").text(), "UTF-8");
		
			link = "http://eventos.showdeingressos.com.br" + link.substring(link.lastIndexOf("/"));
			
			addCapturedLink(link + "?title=" + title + "&date=" + date + "&location=" + location);
		}
	}
}