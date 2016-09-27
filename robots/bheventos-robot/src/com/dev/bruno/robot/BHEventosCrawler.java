package com.dev.bruno.robot;

import java.text.SimpleDateFormat;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class BHEventosCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy");
		DateTime today = new DateTime();
		DateTime lastDate = new DateTime().plusMonths(1);

		while(today.isBefore(lastDate)) {
			String newUrl = url + "/dia/" + dateFormat.format(today.toDate());
			Long time = System.currentTimeMillis();
			
			Document body = documentService.getDocument(newUrl, crawlerDTO.getConnectionTimeout());
			
			for(Element a : body.select("div.block-info > h2 > a")) {
				String link = a.attr("href");
				
				link = "http://www.bheventos.com.br" + link;
				
				if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
					continue;
				}
				
				addCapturedLink(link);
			}
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms.", newUrl, time));
			
			today = today.plusDays(1);
		}
	}
}