package com.dev.bruno.robot;

import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Crawling(documentType=DocumentType.SHOW)
public class EnjooyCrawler extends Crawler {
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		List<String> ufs = new ArrayList<>();
		
		ufs.add("ac");
		ufs.add("al");
		ufs.add("am");
		ufs.add("ap");
		ufs.add("ba");
		ufs.add("ce");
		ufs.add("df");
		ufs.add("es");
		ufs.add("go");
		ufs.add("ma");
		ufs.add("mg");
		ufs.add("ms");
		ufs.add("mt");
		ufs.add("pa");
		ufs.add("pb");
		ufs.add("pe");
		ufs.add("pi");
		ufs.add("pr");
		ufs.add("rj");
		ufs.add("rn");
		ufs.add("ro");
		ufs.add("rr");
		ufs.add("rs");
		ufs.add("sc");
		ufs.add("se");
		ufs.add("sp");
		ufs.add("to");
		
		Set<String> urls = new HashSet<>();
		
		for(String uf : ufs) {
			Long time = System.currentTimeMillis();
			
			Document body = null;
			try {
				body = documentService.getDocument(url + "/" + uf, crawlerDTO.getConnectionTimeout());
			} catch(SocketTimeoutException e) {
				logger.log(Level.SEVERE, "ERRO NO ACESSO[TIME_OUT] AO LINK: " + url + "/" + uf);
				continue;
			}
			
			for(Element a : body.select("div.nome-cidade > a.name")) {
				String link = a.attr("href");
				
				String municipio = URLEncoder.encode(StringUtils.clearText(a.text().toUpperCase()), "UTF-8");
				
				urls.add(link + "_SEPARATOR_" + municipio + "_SEPARATOR_" + uf.toUpperCase());
			}
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms.", url + "/" + uf, time));
		}
		
		Integer counter = 0;
		for(String newUrl : urls) {
			Long time = System.currentTimeMillis();
			
			String municipio = newUrl.split("_SEPARATOR_")[1];
			String uf = newUrl.split("_SEPARATOR_")[2];
			newUrl = newUrl.split("_SEPARATOR_")[0].replaceAll("br\\.enjooy\\.com\\/", "br.enjooy.com/events/");
			
			Document body = null;
			try {
				body = documentService.getDocument(newUrl, crawlerDTO.getConnectionTimeout());
			} catch(SocketTimeoutException e) {
				logger.log(Level.SEVERE, "ERRO NO ACESSO[TIME_OUT] AO LINK: " + newUrl);
				continue;
			}
			
			for(Element a : body.select("div.capa > a")) {
				String link = a.attr("href");
				
				if(link.matches(crawlerDTO.getDocumentURLRegex())) {
					addCapturedLink(link + "?municipio=" + municipio + "&uf=" + uf);
				}
			}
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms. --> %s/%s", newUrl, time, ++counter, urls.size()));
		}
	}
}