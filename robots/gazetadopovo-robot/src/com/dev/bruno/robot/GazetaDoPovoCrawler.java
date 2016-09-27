package com.dev.bruno.robot;

import java.util.ArrayList;
import java.util.List;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class GazetaDoPovoCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		List<String> urls = new ArrayList<>();
		
		urls.add("?pag=0&order=1&Cidade_ID_RM=1&Cidade_Nome_RM=Curitiba%20e%20Regi%E3o&aba=77&diaSelecionado=&semana=#");
		urls.add("?pag=1&order=1&Cidade_ID_RM=1&Cidade_Nome_RM=Curitiba%20e%20Regi%E3o&aba=77&diaSelecionado=&semana=#");
		urls.add("?pag=2&order=1&Cidade_ID_RM=1&Cidade_Nome_RM=Curitiba%20e%20Regi%E3o&aba=77&diaSelecionado=&semana=#");
		
		for(String newUrl : urls)  {
			Document body = documentService.getDocument(url + newUrl, crawlerDTO.getConnectionTimeout());
			
			for(Element a : body.select("div.listagem h4 a")) {
				String link = a.attr("href");
				
				if(!link.matches(crawlerDTO.getDocumentURLRegex())) {
					continue;
				}
				
				addCapturedLink(link);
			}
		}
	}
}