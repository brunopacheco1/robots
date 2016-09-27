package com.dev.bruno.robot;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class TicketsForFunNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		if(!body.select("img.tryAgainButton").isEmpty()) {
			logger.info("TRÁFEGO INTENSO NO SITE[" + url + "]!");
			return;
		}
		
		List<String> urls = new ArrayList<>();
		for(Element a : body.select("a")) {
			String link = a.attr("href");
			
			if(link.matches("\\.\\.\\/shows\\/show\\.aspx\\?sh=.+?&v=.+")) {
				String uf = a.parent().parent().select("div.stateinfo").text();
				String municipio = a.parent().parent().select("div.eventsinfo > p > small").text();
				String newUrl = "http://premier.ticketsforfun.com.br"  + link.replaceAll("\\.\\.\\/", "/") + "&uf=" + uf + "&municipio=" + URLEncoder.encode(municipio, "UTF-8");
				urls.add(newUrl);
			}
		}
		
		if(urls.isEmpty()) {
			urls.add(url);
		}
		
		Boolean found = false;
		
		for(String newUrl : urls) {
			if(!newUrl.equals(newUrl)) {
				body = documentService.getDocument(newUrl, normalizerDTO.getConnectionTimeout());
			}
			
			String [] atributos = newUrl.split("\\?")[1].split("&");
			
			String uf = null;
			String municipio = null;
			
			for(String atributo : atributos) {
				if(atributo.startsWith("uf=")) {
					uf = atributo.split("=")[1];
				} else if(atributo.startsWith("municipio=")) {
					municipio = StringUtils.clearText(URLDecoder.decode(atributo.split("=")[1],"UTF-8").toUpperCase());
				}
			}
			
			uf = ufs.get(uf);
			
			if(uf == null || municipio == null) {
				uf = null;
				municipio = null;
			}
			
			String title = body.select("div.sectionTitle").text();
			
			if(title == null || title.isEmpty()) {
				continue;
			}
			
			if(body.select("div.boxRelease div.firstSection small") == null || body.select("div.boxRelease div.firstSection small").isEmpty()) {
				continue;
			}
			
			String local = body.select("div.boxRelease div.firstSection small").first().text();
	
			if(local == null || local.isEmpty()) {
				continue;
			}
			
			List<String> dates = new ArrayList<>();
			for(Element option : body.select("select > option[value~=.+]")) {
				dates.add(option.text());
			}
			
			if(dates.isEmpty()) {
				String dateStr = Jsoup.parse(body.select("div.buyTicketsInformation").html().split("Data: </strong></span>")[1].split("<input")[0]).text().trim();
				dates.add(dateStr);
			}
			
			for(String dateStr : dates) {
				if(dateStr == null || dateStr.isEmpty()) {
					return;
				}
				
				String [] dateArray = dateStr.split("\\s");
				
				dateStr = dateArray[1] + " " + dateArray[2];
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH':'mm'HS 'dd'-'MMM'-'yyyy", new Locale("pt", "BR"));
				
				Date realizacao = null;
				try {
					realizacao = dateFormat.parse(dateStr);
				} catch (ParseException e) {
					dateFormat = new SimpleDateFormat("HH':'mm' 'dd'-'MMM'-'yyyy", new Locale("pt", "BR"));
					
					try {
						realizacao = dateFormat.parse(dateStr);
					} catch (ParseException e1) {
						continue;
					}
				}
				
				ShowDTO show = new ShowDTO();
				show.setNome(title);
				show.setLocalCaptado(local);
				show.setUrlBase(newUrl);
				show.setDataRealizacao(realizacao);
				show.setUf(uf);
				show.setMunicipio(municipio);
				
				addCapturedDocument(show);
				
				found = true;
			}
		}
		
		if(!found) {
			setProblemLink(true);
		}
	}
}