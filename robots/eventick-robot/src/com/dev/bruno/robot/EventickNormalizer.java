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

@Normalizing(documentType=DocumentType.SHOW)
public class EventickNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("meta[itemprop=name]").attr("content");
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("span.hd-event-local").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = Jsoup.parse(body.select("h4[itemprop=address]").html().split("<br>")[0]).text();
		String uf = null;
		String municipio = null;
		
		if(endereco != null && !endereco.isEmpty()) {
			Map<String, String> resultado = GoogleUtils.findAddress(endereco);
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
		if(uf == null || municipio == null) {
			Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
			
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String date = body.select("meta[itemprop=startDate]").attr("content");

		if(date == null || date.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		date = date.substring(0, date.lastIndexOf("-"));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH:mm:ss");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(date);
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