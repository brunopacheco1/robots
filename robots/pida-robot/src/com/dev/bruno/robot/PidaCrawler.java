package com.dev.bruno.robot;

import java.net.URLEncoder;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class PidaCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element div : body.select("div.box-agenda")) {
			String link = "http://www.pida.com.br/" + div.select("a").attr("href");
			
			String title = URLEncoder.encode(div.select("h3.txt-cinza-agenda").text(), "UTF-8");
			String local = URLEncoder.encode(div.select("h5.txt-cinza").text(), "UTF-8");
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			addCapturedLink(link + "?title=" + title + "&local=" + local);
		}
	}
}