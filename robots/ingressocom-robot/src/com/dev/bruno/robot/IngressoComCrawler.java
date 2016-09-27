package com.dev.bruno.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

@Crawling(documentType=DocumentType.SHOW)
public class IngressoComCrawler extends Crawler {
	
	@SuppressWarnings("unchecked")
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Gson gson = new Gson();
		
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());

		List<String> cidadesToVisit = new ArrayList<>();
		
		for(Element script : body.select("script")) {
			if(script.html().contains("var estados")) {
				String estadosJson = script.html().substring(script.html().indexOf("\'") + 1, script.html().indexOf("\';\r\n"));
				Map<String, Object> estados = gson.fromJson(estadosJson, HashMap.class);
				
				for(String estado : estados.keySet()) {
					List<Map<String, Object>> cidades = (List<Map<String, Object>>) estados.get(estado);
					
					for(Map<String, Object> cidade : cidades) {
						cidadesToVisit.add((String) cidade.get("ChaveUrl"));
					}
				}
				
				break;
			}
		}
		
		for(String cidade : cidadesToVisit) {
			Long time = System.currentTimeMillis();
			
			String cidadeUrl = "http://www.ingresso.com/" + cidade + "/home/filtro/recuperadadosfiltro?tipoevento=show";
			
			String jsonStr = documentService.getDocumentAsString(cidadeUrl, crawlerDTO.getConnectionTimeout());
			
			Map<String, Object> json = gson.fromJson(jsonStr, HashMap.class);
			
			List<Map<String, Object>> espetaculos = (List<Map<String, Object>>) json.get("Espetaculos");
			
			Long counter = 0l;
			
			for(Map<String, Object> espetaculo : espetaculos) {
				
				String newUrl = "http://www.ingresso.com/" + cidade + "/home/espetaculo/show/" + espetaculo.get("Id");
				
				counter++;
				
				addCapturedLink(newUrl);
			}
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms and %s captured urls.", cidadeUrl, time, counter));
		}
	}
}