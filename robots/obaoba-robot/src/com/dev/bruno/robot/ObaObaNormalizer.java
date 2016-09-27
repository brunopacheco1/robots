package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class ObaObaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.title > h1.p-name").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("section.tools-estabelecimento > p.onde a.p-label").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = body.select("section.tools-estabelecimento > p.date").text();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.split(":")[1].trim();
		
		String hourStr = body.select("section.tools-estabelecimento > p.clock").text();
		
		if(hourStr == null || hourStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		hourStr = hourStr.replaceAll("\\D", "");
		
		if(hourStr.length() == 2) {
			hourStr += "00";
		}
		
		String endereco = body.select("section.tools-estabelecimento > p.onde > span.p-location > span.p-street-address").text();
		String cidade = body.select("section.tools-estabelecimento > p.onde > span.p-location > span.p-locality").text();
		
		String uf = null;
		String municipio = null;

		if(endereco != null && cidade != null) {
			endereco += ", " + cidade;
		} else if(cidade != null) {
			endereco = cidade;
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
		
		Set<String> dates = new HashSet<>();
		
		if(dateStr.matches("^(\\d{1}|\\d{2})\\sde\\s.+\\sde\\s\\d{4}$")) {
			dates.add(dateStr);
		} else if(dateStr.matches("^(\\d{1}|\\d{2})\\se\\s(\\d{1}|\\d{2})\\sde\\s.+\\sde\\s\\d{4}$")) {
			String endDate = dateStr.substring(dateStr.indexOf("e") + 1).trim();
			String startDate = dateStr.substring(0, dateStr.indexOf("e")).trim() + " " + dateStr.substring(dateStr.indexOf("de")).trim();
			dates.add(endDate);
			dates.add(startDate);
		} else {
			setProblemLink(true);
			return;
		}
		
		for(String newDateStr : dates) {
			newDateStr += " " + hourStr;
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy' 'HHmm", new Locale("pt", "BR"));
			
			Date realizacao = null;
			try {
				realizacao = dateFormat.parse(newDateStr);
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
}