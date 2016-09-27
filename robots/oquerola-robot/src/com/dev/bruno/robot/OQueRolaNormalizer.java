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
public class OQueRolaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.page-title > div.text > span").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("div.event-text > p").isEmpty() || !body.select("div.event-text > p").first().text().contains("Local:")) {
			setProblemLink(true);
			return;
		}
		
		String local = Jsoup.parse(body.select("div.event-text > p").first().html().split("<br>")[0]).text().split("Local:")[1].trim();
		
		String dateStr = body.select("div.event-date > div.left > span").text();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.replaceAll("\\D", "") + " 0000";
		
		if(dateStr.length() != 13) {
			setProblemLink(true);
			return;
		}
		
		Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
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