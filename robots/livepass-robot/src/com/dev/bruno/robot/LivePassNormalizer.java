package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class LivePassNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		if(body.select("p.event-name").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String title = body.select("p.event-name").first().text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		for(Element div : body.select("div.dados div.view-dados")) {
			String local = div.select("p.local").text();

			if(local == null || local.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			String date = div.select("p.date").text();
			
			if(date == null || date.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			String endereco = div.select("p.address").text();
			String municipio = div.select("p.city").text();
			String uf = null;
			
			if(municipio != null && !municipio.isEmpty()) {
				municipio = municipio.toUpperCase().trim();
			} else {
				municipio = null;
			}
			
			if(endereco != null && !endereco.isEmpty() && municipio != null && !municipio.isEmpty()) {
				endereco += ", " + municipio;
				
				Map<String, String> resultado = GoogleUtils.findAddress(endereco);
				
				municipio = resultado.get("municipio");
				uf = resultado.get("uf");
			} else {
				municipio = null;
			}
			
			if(uf == null || municipio == null) {
				Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
				
				uf = resultado.get("uf");
				municipio = resultado.get("municipio");
			}
			
			if(uf == null || municipio == null) {
				uf = null;
				municipio = null;
			}
			
			String hour = div.select("div.show-time-row").text();
	
			if(hour == null || hour.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			hour = hour.replaceAll("\\D", "");
			
			String earlyHour = hour.substring(0, 4);
			
			String lateHour = hour.substring(hour.length() - 4);
			
			String earlyDate = date + " " + earlyHour;
			String lateDate = date + " " + lateHour;
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'HHmm");
			
			DateTime earlyRealizacao = null;
			try {
				earlyRealizacao = new DateTime(dateFormat.parse(earlyDate));
			} catch (ParseException e) {
				logger.info("Erro no recorte de data, página fora do padrão.");
				setProblemLink(true);
				return;
			}
			
			DateTime lateRealizacao = null;
			try {
				lateRealizacao = new DateTime(dateFormat.parse(lateDate));
			} catch (ParseException e) {
				logger.info("Erro no recorte de data, página fora do padrão.");
				setProblemLink(true);
				return;
			}
			
			if(lateRealizacao.isBefore(earlyRealizacao)) {
				lateRealizacao = lateRealizacao.plusDays(1);
			}
			
			Date realizacao = lateRealizacao.toDate();
			
			ShowDTO show = new ShowDTO();
			show.setNome(title);
			show.setLocalCaptado(local);
			show.setUrlBase(url);
			show.setDataRealizacao(realizacao);
			show.setMunicipio(municipio);
			show.setUf(uf);
			
			addCapturedDocument(show);
		}
	}
}