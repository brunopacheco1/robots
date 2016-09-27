package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class GuiaDaSemanaNormalizer extends ShowNormalizer {
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1#main-title").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("li.event-map a.local").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = null;
		if(body.select("li.event-map").html().contains("<br>")) {
			endereco = body.select("li.event-map").html().split("<br>")[1].split("\\|")[0].trim();
		}
		
		Map<String, String> resultado = GoogleUtils.findAddress(endereco);
		if(resultado.isEmpty()) {
			resultado = GoogleUtils.findAddressByPlace(local);
		}
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String dateStr = url.substring(url.length() - 10);

		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String hourStr = body.select("li.event-hour").text();

		if(hourStr == null || hourStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		try {
			hourStr = hourStr.split(":")[1].trim();
			if(hourStr.contains(";")) {
				hourStr = hourStr.split(";")[0].trim().replaceAll("\\D", "");
			}
		} catch(Exception e) {
			logger.info("Erro no recorte de horário de abertura, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		dateStr += " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'-'MM'-'yyyy' 'HH");
		if(dateStr.matches("^\\d{2}-\\d{2}-\\d{4}\\s\\d{4}$")) {
			dateFormat = new SimpleDateFormat("dd'-'MM'-'yyyy' 'HHmm");
		}
		
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