package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class TurismoRSNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.tituloEvento").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = null;
		String dateStr = null;
		String uf = "RS";
		String municipio = null;
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		for(Element item : body.select("div.cFormularioItem")) {
			String text = item.select("div.cFormularioItemLabel").text().toLowerCase();
			
			if(text.contains("data início")) {
				dateStr = item.select("div.cFormularioItemDado").text();
			} if(text.contains("local")) {
				local = item.select("div.cFormularioItemDado").text();
			} else if(text.contains("cidade")) {
				municipio = StringUtils.clearText(item.select("div.cFormularioItemDado").text().toUpperCase());
			}
		}
		
		if(dateStr == null || local == null || municipio == null) {
			setProblemLink(true);
			return;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy");
		
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