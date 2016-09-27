package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class UsinaDoIngressoNormalizer extends ShowNormalizer {
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout(), "ISO-8859-1", true);
		
		String title = body.select("div#produtos > h2").html();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = null;
		String local = null;
		String endereco = null;
		String hourStr = null;
		String uf = null;
		String municipio = null;
		
		for(String text : body.select("div.box-preco.clearfix > p").html().split("<br>")) {
			if(text.toLowerCase().contains("data")) {
				dateStr = text.replaceAll("<.*?>", "").replaceAll("\\D", "");
			} else if(text.toLowerCase().contains("horário")) {
				hourStr = text.replaceAll("<.*?>", "").trim().replaceAll("\\D", "");
			} else if(text.toLowerCase().contains("local")) {
				local = text.replaceAll("<.*?>", "").split(":")[1].trim();
			} else if(text.toLowerCase().contains("cidade")) {
				endereco = text.replaceAll("<.*?>", "").split(":")[1].trim();
				
				uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim();
				municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim());
			}
		}
		
		if(dateStr == null || local == null || hourStr == null || uf == null || municipio == null) {
			setProblemLink(true);
			return;
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		if(hourStr.length() == 2) {
			hourStr += "00";
		}
		
		dateStr += " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy' 'HHmm");
		
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