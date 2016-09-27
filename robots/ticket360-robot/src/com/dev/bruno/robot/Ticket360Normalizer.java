package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class Ticket360Normalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h4.media-heading").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.media-body.eventos strong").first().text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		Element parent = body.select("div.media-body.eventos strong").first().parent();
		
		parent.select("strong").remove();
		
		String endereco = parent.text();
		
		String uf = null;
		String municipio = null;

		if(endereco != null && !local.isEmpty()) {
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
		
		String dateStr = body.select("p.data").text();

		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		try {
			dateStr = dateStr.split(",")[1].replaceAll("Abertura: ", "").replaceAll("Início: ", "").trim();
		} catch(Exception e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		try {
			dateStr = dateStr.split("-")[0].trim() + " " + dateStr.split("-")[dateStr.split("-").length - 1].trim();
		} catch(Exception e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' 'HH':'mm", new Locale("pt", "BR"));
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		DateTime realizacaoDt = new DateTime(realizacao);
		realizacaoDt = realizacaoDt.withYear(new DateTime().getYear());

		if(realizacaoDt.isBeforeNow()) {
			realizacaoDt = realizacaoDt.withYear(realizacaoDt.getYear() + 1);
		}
		
		realizacao = realizacaoDt.toDate();
		
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