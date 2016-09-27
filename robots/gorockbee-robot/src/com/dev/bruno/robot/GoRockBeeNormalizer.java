package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class GoRockBeeNormalizer extends ShowNormalizer {
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("meta[property=og:title]").attr("content");
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("section.rowline h3 > a").text();
		String uf = null;
		String municipio = null;
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		municipio = local.split(" / ")[1];
		
		uf = municipio.substring(municipio.lastIndexOf("-") + 1).trim();
		
		municipio = municipio.substring(0, municipio.lastIndexOf("-")).trim();

		local = local.split(" / ")[0].trim();
		
		for(Element sectionDate : body.select("section.date-container")) {
			String dateStr = sectionDate.select("span.date-day").text() + " / " + sectionDate.select("span.date-month").text() + " " + sectionDate.select("span.date-hour").text();
	
			if(dateStr == null || dateStr.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			uf = ufs.get(uf);
			
			if(uf == null || municipio == null) {
				uf = null;
				municipio = null;
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd' / 'MMM' / 'yyyy' 'HH'h'mm", new Locale("pt", "BR"));
			
			Date realizacao = null;
			try {
				realizacao = dateFormat.parse(dateStr.toLowerCase());
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