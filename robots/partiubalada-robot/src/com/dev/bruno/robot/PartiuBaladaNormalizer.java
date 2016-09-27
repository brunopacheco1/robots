package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class PartiuBaladaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1#title").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String desc = body.select("div.blockPadding > div").html();
		
		String local = null;
		String endereco = null;
		String dateStr = null;
		String hourStr = null;
		String uf = null;
		String municipio = null;
		
		for(String line : desc.split("<br>")) {
			line = Jsoup.parse(line).text();
			if(line.toLowerCase().contains("data")) {
				dateStr = line.replaceAll("\\D", "");
			} else  if(line.toLowerCase().contains("horário")) {
				hourStr = line.replaceAll("\\D", "");
			} else if(line.toLowerCase().contains("local")) {
				local = line.split(":")[1].trim();
			} else if(line.toLowerCase().contains("endereço")) {
				endereco = line.split(":")[1].trim();
			}
			
			if(dateStr != null && hourStr != null && local != null && endereco != null) {
				break;
			}
		}
		
		if(dateStr == null || hourStr == null || local == null) {
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
		
		dateStr += " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy' 'HHmmss");
		
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