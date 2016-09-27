package com.dev.bruno.robot;

import java.net.URLEncoder;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class PrimeIngressosCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());
		
		for(Element div : body.select("div#listaevento fieldset")) {
			String link = div.select("input[type=submit]").attr("onclick");
			
			if(!link.contains("detalhesitens.aspx?cod_evento=")) {
				continue;
			}
			
			String title = URLEncoder.encode(div.select("ul > li").get(0).text(), "UTF-8");
			String date = URLEncoder.encode(div.select("ul > li").get(1).text(), "UTF-8");
			String location = URLEncoder.encode(div.select("ul > li").get(2).text(), "UTF-8");
			
			link = "http://www.primeingressos.com.br/detalhesitens.aspx?cod_evento=" + link.split("cod_evento=")[1].split("\"")[0];
			
			if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
				continue;
			}
			
			addCapturedLink(link + "&title=" + title + "&date=" + date + "&location=" + location);
		}
	}
}