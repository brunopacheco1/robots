package com.dev.bruno.robot;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.DocumentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

@Crawling(documentType=DocumentType.SHOW)
public class SymplaCrawler extends Crawler {
	
	@SuppressWarnings("unchecked")
	public void run(CrawlerDTO crawlerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, crawlerDTO.getConnectionTimeout());

		getLinks(body, crawlerDTO.getDocumentURLRegex());
		
		Long total = Long.parseLong(body.select("div.item-results").text().replaceAll("\\D", ""));
		
		Long limit = 20l;
		
		Integer page = 2;
		for(Long start = limit; start < total; start += limit) {
			Long time = System.currentTimeMillis();
			
			URL obj = new URL("http://www.sympla.com.br/site/moreSearchResults");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			con.setRequestProperty("Host", "www.sympla.com.br");
			con.setRequestProperty("Origin", "http://www.sympla.com.br");
			con.setRequestProperty("Referer", url);
			
			String urlParameters = "e=&c=9&s=&start=" + start.toString() + "&date1=&date2=&limit=" + limit.toString() + "&total=" + total.toString() + "&isEventsFree=0";
			
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			String response = IOUtils.toString(con.getInputStream());
			
			Gson gson = new Gson();
			
			Map<String, Object> result = gson.fromJson(response, Map.class);
			
			List<Object> shows = (List<Object>) result.get("data");
			
			getLinks(Jsoup.parse(StringUtils.join(shows, "")), crawlerDTO.getDocumentURLRegex());
			
			time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - 1/1] visitada em %sms.", url + "#" + page, time));
			
			page++;
		}
	}
	
	private void getLinks(Document body, String regex) {
		for(Element a : body.select("a.event-box-link")) {
			String link = a.attr("href");
			
			if(link == null || link.isEmpty()) {
				continue;
			}
			
			if(!link.matches(regex)) {
				continue;
			}
			
			addCapturedLink(link);
		}
	}
}