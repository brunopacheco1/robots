package com.dev.bruno.robot;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class ObaObaCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		Set<String> urls = new HashSet<>();
		
		for(Element a : body.select("div#local-list ul > li > a")) {
			String link = "http://www.obaoba.com.br" + a.attr("href") + "/agendadodia/";
			
			DateTime today = new DateTime();
			DateTime lastDate = today.plusWeeks(2);
			
			while(today.isBefore(lastDate)) {
				urls.add(link + today.toString("dd-MM-yyyy"));
				
				today = today.plusDays(1);
			}
		}
		
		for(String newUrl : urls) {
			Long time = System.currentTimeMillis();
			
			body = null;
			try {
				body = documentService.getDocument(newUrl, crawlerDTO.getConnectionTimeout());
			} catch(Exception e) {}
			
			if(body == null) {
				logger.log(Level.SEVERE, "ERRO NA CAPTACAO DO LINK: " + newUrl);
				continue;
			}
			
			for(Element a : body.select("div.image-agenda-lista > a")) {
				String link = "http://www.obaoba.com.br" + a.attr("href");
				
				if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
					continue;
				}
				
				addCapturedLink(link);
			}
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms.", newUrl, time));
		}
	}
}