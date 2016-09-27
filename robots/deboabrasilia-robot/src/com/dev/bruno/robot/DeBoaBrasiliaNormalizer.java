package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class DeBoaBrasiliaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Map<String, String> months = new HashMap<>();
		
		months.put("janeiro", "01");
		months.put("fevereiro", "02");
		months.put("março", "03");
		months.put("abril", "04");
		months.put("maio", "05");
		months.put("junho", "06");
		months.put("julho", "07");
		months.put("agosto", "08");
		months.put("setembro", "09");
		months.put("outubro", "10");
		months.put("novembro", "11");
		months.put("dezembro", "12");
		
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1.page-header").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String desc = body.select("div.descricao-interno-post").text();
		
		if(desc == null || desc.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(!desc.contains("Local:")) {
			setProblemLink(true);
			return;
		}
		
		if(!desc.contains("Data:")) {
			setProblemLink(true);
			return;
		}
		
		if(!desc.contains("Hora:")) {
			setProblemLink(true);
			return;
		}
		
		String local = desc.substring(desc.lastIndexOf("Local:")).trim().split(":")[1].trim();
		
		String dateStr = desc.substring(desc.lastIndexOf("Data:"), desc.lastIndexOf("Hora:")).trim();
		
		Integer day = null;
		String month = null;
		
		for(String slice : dateStr.split("\\s")) {
			slice = slice.replaceAll("\\.", "");
			slice = slice.replaceAll("\\,", "").toLowerCase();
			
			if(slice.matches("^\\d+$")) {
				Integer newDay = Integer.parseInt(slice);
				
				if(day == null || day > newDay) {
					day = newDay;
				}
			} else if(months.containsKey(slice)) {
				month = months.get(slice);
			}
		}
		
		if(day == null || month == null) {
			setProblemLink(true);
			return;
		}
		
		String hourStr = desc.substring(desc.lastIndexOf("Hora:"), desc.lastIndexOf("Local:")).trim();
		
		Integer hour = null;
		Integer minutes = null;
		
		for(String slice : hourStr.split("\\s")) {
			slice = slice.replaceAll("\\.", "");
			slice = slice.replaceAll("\\,", "");
			
			if(slice.matches("^\\d+h$")) {
				Integer newHour = Integer.parseInt(slice.replaceAll("\\D", ""));
				
				if(hour == null || hour > newHour) {
					hour = newHour;
					
					minutes = 0;
				}
			} else if(slice.matches("^\\d+h\\d+$")) {
				Integer newHour = Integer.parseInt(slice.split("h")[0]);
				Integer newMinutes = Integer.parseInt(slice.split("h")[1]);
				
				if(hour == null || hour > newHour) {
					hour = newHour;
					minutes = newMinutes;
				}
			}
		}
		
		if(hour == null) {
			setProblemLink(true);
			return;
		}
		
		dateStr = day + "/" + month + "/" + new DateTime().getYear() + " " + hour + ":" + minutes;
		
		
		
		String uf = null;
		String municipio = null;
		
		Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
		
		uf = resultado.get("uf");
		municipio = resultado.get("municipio");
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		Date realizacao = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'HH':'m");

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