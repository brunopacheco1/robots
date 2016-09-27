package com.dev.bruno.robot;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;

import com.google.gson.Gson;

@Crawling(documentType=DocumentType.SHOW)
public class IngresseCrawler extends Crawler {
	
	private List<String> ufs = new ArrayList<>();
	
	private IngresseCrawler() {
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
	}
	
	@SuppressWarnings("unchecked")
	private void crawlerState(CrawlerDTO crawlerDTO, String uf, Integer limit, Integer start) throws Exception {
		Gson gson = new Gson();
		
		URL urlEspetaculos = new URL("https://elastic.ingresse.com/events/_search?q=state:" + uf + "&sort=date.dateTime.date:asc&size=" + limit + "&from=" + start);
		
		URLConnection conn = urlEspetaculos.openConnection();
		conn.setReadTimeout(crawlerDTO.getConnectionTimeout().intValue());
		
		Map<String, Object> resultado = gson.fromJson(IOUtils.toString(conn.getInputStream()), HashMap.class);
		
		if(!resultado.containsKey("hits")) {
			return;
		}
		
		Map<String, Object> hits = (Map<String, Object>) resultado.get("hits");
		
		Double totalResult = (Double) hits.get("total");
		
		if(totalResult == 0d || !hits.containsKey("hits")) {
			return;
		}
		
		List<Map<String, Object>> hitsResult = (List<Map<String, Object>>) hits.get("hits");
		
		for(Map<String, Object> hit : hitsResult) {
			if(!hit.containsKey("_source")) {
				continue;
			}
			
			String id = (String) hit.get("_id");
			Map<String, Object> source = (Map<String, Object>) hit.get("_source");
			
			String link = (String) source.get("link");
			
			link = "https://www.ingresse.com/" + link + "?id=" + id;
			
			addCapturedLink(link);
		}
		
		Integer next = start + limit;
		
		if(next < totalResult) {
			crawlerState(crawlerDTO, uf, limit, next);
		}
	}
	
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		for (String uf : ufs) {
			Long time = System.currentTimeMillis();
			
			crawlerState(crawlerDTO, uf, 80, 0);
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms.", "https://www.ingresse.com/search#" + uf, time));
		}
	}
}