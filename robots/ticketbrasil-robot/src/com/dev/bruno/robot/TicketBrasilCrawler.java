package com.dev.bruno.robot;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class TicketBrasilCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Connection connection = Jsoup.connect("https://sis.ticketbrasil.com.br/lojanew/lista_evento.asp?tploja=s&us");
		connection.followRedirects(true);
		connection.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		connection.header("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.6,en;q=0.4");
		connection.header("Cache-Control", "max-age=0");
		connection.header("Connection", "keep-alive");
		connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
		connection.timeout(crawlerDTO.getConnectionTimeout().intValue());
		
		Document body = connection.data("gen_id", "63").post();
		
		for(Element a : body.select("a")) {
			String link = "http://sis.ticketbrasil.com.br/lojanew/" + a.attr("href");
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			addCapturedLink(link);
		}
	}
}