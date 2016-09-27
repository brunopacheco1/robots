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
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class GazetaDoPovoNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.tituloz2 > h2").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.direit2 > p > a").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = null;
		String dateStr = null;
		String uf = null;
		String municipio = null;
		
		for(Element strong : body.select("div.direit2 > p")) {
			String text = Jsoup.parse(strong.html().replaceAll("&nbsp;", "")).text();
			if(text.toLowerCase().contains("quando")) {
				dateStr = text.substring(text.indexOf(":") + 1).trim().replaceAll("\\D", "");
			} else if(text.toLowerCase().contains("onde")) {
				local = text.substring(text.indexOf(":") + 1).trim();
			} else if(text.toLowerCase().contains("endere")) {
				endereco = text.substring(text.indexOf(":") + 1).trim();
			}
		}
		
		if(dateStr == null || local == null) {
			setProblemLink(true);
			return;
		}
		
		if(endereco != null) {
			endereco = endereco.substring(0, endereco.lastIndexOf(","));
			
			Map<String, String> resultado = GoogleUtils.findAddress(endereco);
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}

		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy' 'HHmm");
		
		if(dateStr.length() == 10) {
			dateStr += "00";
		}
		
		dateStr = dateStr.substring(0, 8) + " " + dateStr.substring(8, 12);
		
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