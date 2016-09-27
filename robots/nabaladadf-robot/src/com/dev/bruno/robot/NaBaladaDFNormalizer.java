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
import com.dev.bruno.utils.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class NaBaladaDFNormalizer extends ShowNormalizer {

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
		
		String title = body.select("div.post > h1").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("div.entry > p").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String desc = Jsoup.parse(body.select("div.entry > p").html().replaceAll("&nbsp;", "")).text();
		
		if(!desc.contains("Local:")) {
			desc = Jsoup.parse(body.select("div.entry > address").html().replaceAll("&nbsp;", "")).text();
		}
		
		String local = desc.split("Local:")[1];
		
		String uf = null;
		String municipio = null;
		
		if(StringUtils.clearText(local).toUpperCase().contains("BRASILIA") || local.contains("DF")) {
			uf = "DF";
			municipio = "BRASILIA";
		} else if(StringUtils.clearText(local).toUpperCase().contains("GOIANIA")) {
			uf = "GO";
			municipio = "GOIANIA";
		}
		
		if(local.indexOf("–") != -1) {
			local = local.substring(0,local.indexOf("–")).trim();
		} else if(local.indexOf("-") != -1) {
			local = local.substring(0,local.indexOf("-")).trim();
		} else if(local.indexOf(",") != -1){
			local = local.substring(0,local.indexOf(",")).trim();
		} else if(local.indexOf(".") != -1){
			local = local.substring(0,local.indexOf(".")).trim();
		}
		
		
		String dateStr = desc.split("Data:")[1].split("Hor(a|ário):")[0].trim();
		String newDateStr = "";
		
		for(String slice : dateStr.split("\\s")) {
			slice = slice.replaceAll("\\.|,|de", "");
			
			if(months.containsKey(slice.toLowerCase())) {
				newDateStr += months.get(slice.toLowerCase());
			} else {
				newDateStr += slice;
			}
		}
		
		dateStr = newDateStr.replaceAll("\\D", "");
		Boolean yearAdded = false;
		
		if(dateStr.length() == 4) {
			yearAdded = true;
			dateStr += new DateTime().getYear();
		}
		
		String hourStr = desc.split("Hor(a|ário):")[1].split("Local:")[0].trim().replaceAll("\\D", "");
		
		if(hourStr.length() == 2) {
			hourStr += "00";
		}
		
		dateStr += " " + hourStr;
		
		if(!dateStr.matches("\\d{8}\\s\\d{4}")) {
			setProblemLink(true);
			return;
		}
		
		if(uf == null) {
			Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy' 'HHmm");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		if(yearAdded && realizacao.before(new Date())) {
			dateStr = dateStr.substring(0, 4) + (new DateTime().getYear() + 1) + " " + dateStr.substring(9, 13);
			
			realizacao = dateFormat.parse(dateStr);
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