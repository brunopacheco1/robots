package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dev.bruno.utils.GoogleUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class BHEventosNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.events > h1").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String [] slices = url.split("\\/")[4].split("-");
		
		String dateStr = slices[1] + "/" + slices[0] + "/" + slices[2];
		
		String local = null;
		String endereco = null;
		String hourStr = null;
		String uf = null;
		String municipio = null;
		
		for(Element strong : body.select("div#about-event > p > strong")) {
			if(strong.text().toLowerCase().contains("horário")) {
				hourStr = strong.parent().text().split(":")[1].trim();
			} else if(strong.text().toLowerCase().contains("local")) {
				local = strong.parent().text().split(":")[1].trim();
			} else if(strong.text().toLowerCase().contains("endere")) {
				endereco = strong.parent().text().split(":")[1].trim();
			}
		}
		
		if(local == null) {
			setProblemLink(true);
			return;
		}
		
		if(endereco != null) {
			Map<String, String> resultado = GoogleUtils.findAddress(endereco);
			
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		Integer endHour = null;
		Integer endMinutes = null;
		
		if(hourStr != null) {
			for(String hour : hourStr.split("\\s")) {
				hour = hour.replaceAll("\\.", "");
				hour = hour.replaceAll(",", "");
				
				if(hour.toLowerCase().matches("^\\d+h$")) {
					Integer newHour = Integer.parseInt(hour.replaceAll("\\D", ""));
					
					if(endHour == null || endHour > newHour) {
						endHour = newHour;
					}
				} else if(hour.toLowerCase().matches("^\\d+h\\d+$")) {
					Integer newHour = Integer.parseInt(hour.split("h")[0].trim());
					Integer newMinutes = Integer.parseInt(hour.split("h")[1].trim());
					
					if(endHour == null || endHour > newHour) {
						endHour = newHour;
						endMinutes = newMinutes;
					}
				}
			}
		}
		
		if(endHour == null) {
			endHour = 0;
		}
		
		if(endMinutes == null) {
			endMinutes = 0;
		}
		
		dateStr += " " + endHour + ":" + endMinutes;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'H':'m");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		ShowDTO show = new ShowDTO();
		show.setNome(title);
		show.setLocalCaptado(local);
		show.setUrlBase(url);
		show.setDataRealizacao(realizacao);
		show.setUf(uf);
		show.setMunicipio(municipio);
		
		addCapturedDocument(show);
	}
}