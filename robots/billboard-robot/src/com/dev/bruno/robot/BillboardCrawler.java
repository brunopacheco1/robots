package com.dev.bruno.robot;

import java.util.HashSet;
import java.util.Set;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class BillboardCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Set<String> urls = new HashSet<>();
		
		DateTime today = new DateTime();
		DateTime lastDate = today.plusMonths(2);
		
		while(today.isBefore(lastDate)) {
			urls.add(url + today.toString("yyyy-MM"));
			
			today = today.plusMonths(1);
		}
		
		for(String newUrl : urls) {
			String mes = newUrl.substring(newUrl.lastIndexOf("/") + 1);
			
			Long time = System.currentTimeMillis();
			
			Document body = documentService.getDocument(newUrl, crawlerDTO.getConnectionTimeout());
			
			for(Element a : body.select("a")) {
				String link = a.attr("href");
				
				if(link == null || link.isEmpty()) {
					continue;
				}
				
				if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
					continue;
				}
				
				addCapturedLink(link + "?mes=" + mes);
			}
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms.", newUrl, time));
		}
	}
}