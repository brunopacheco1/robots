package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class GuicheWebNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1.tit_evento").text();
		
		if((title == null || title.isEmpty()) && !body.select("h1").isEmpty()) {
			title = body.select("h1").first().text();
		}
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = null;
		String endereco = null;
		String dateStr = null;
		String hourStr = null;
		String uf = null;
		String municipio = null;
		
		for(Element strong : body.select("strong")) {
			if(strong.text().toLowerCase().contains("data")) {
				dateStr = strong.parent().text();
			} else  if(strong.text().toLowerCase().contains("horário")) {
				hourStr = strong.parent().text();
			} else if(strong.text().toLowerCase().contains("local")) {
				local = strong.parent().text();
			} else if(strong.text().toLowerCase().contains("endere")) {
				endereco = strong.parent().text();
				String [] slices = endereco.split("-");
				endereco = slices[slices.length - 1].trim();
				
				break;
			} else if(strong.text().toLowerCase().contains("cidade")) {
				endereco = strong.parent().text();
				String [] slices = endereco.split(":");
				endereco = slices[slices.length - 1].trim();
				break;
			}
		}
		
		if(dateStr == null || local == null || hourStr == null) {
			setProblemLink(true);
			return;
		}
		
		if(endereco != null) {
			uf = endereco.split("/")[1].trim().toUpperCase();
			municipio = StringUtils.clearText(endereco.split("/")[0].trim().toUpperCase());
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		local = local.split(":")[1].split("-")[0].trim();
		
		dateStr = dateStr.replaceAll("\\D", "");
		
		hourStr = hourStr.replaceAll("\\D", "");
		
		Integer numberOfDates = dateStr.length() / 8;
		
		Set<String> dates = new HashSet<>();
		
		for(int i = 0; i < numberOfDates; i++) {
			int start = i * 8;
			int end = start + 8;
			
			dates.add(dateStr.substring(start, end));
		}
		
		for(String newDateStr : dates) {
			
			newDateStr += " " + hourStr;
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy' 'HHmm");
			
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