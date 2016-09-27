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

@Normalizing(documentType=DocumentType.SHOW)
public class DiskIngressosNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div#nomeDaBanda").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.textoLocal").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		local = local.replaceAll("como chegar\\?", "").trim();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = body.select("div#mapa").attr("onclick");
		
		String uf = null;
		String municipio = null;
		
		if(endereco != null && !endereco.isEmpty()) {
			endereco = endereco.split("'")[1].split("=")[1].trim();

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
		
		String date = body.select("div#dataDoShow").text();

		if(date == null || date.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		date = date.replaceAll("-", "").trim();
		
		String hour = body.select("div.textoHorario").text();
		
		if(hour == null || hour.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String earlyDate = date;
		
		String lateDate = date;
		
		boolean first = true;
		
		for(String hourStr : hour.split("\\s")) {
			hourStr = hourStr.replaceAll("\\D", "");
			if(hourStr.matches("\\d{4}") && first) {
				earlyDate += " " + hourStr;
				first = false;
			} else if(hourStr.matches("\\d{4}")) {
				lateDate += " " + hourStr;
			}
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'HHmm");
		
		Date earlyRealizacao = null;
		try {
			earlyRealizacao = dateFormat.parse(earlyDate);
			
			if(earlyRealizacao.before(new Date())) {
				DateTime realizacaoDt = new DateTime(earlyRealizacao);
				earlyRealizacao = realizacaoDt.plusYears(1).toDate();
			}
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		Date lateRealizacao = null;
		try {
			lateRealizacao = dateFormat.parse(lateDate);
			
			if(lateRealizacao.before(new Date())) {
				DateTime realizacaoDt = new DateTime(lateRealizacao);
				lateRealizacao = realizacaoDt.plusYears(1).toDate();
			}
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		if(lateRealizacao.before(earlyRealizacao)) {
			DateTime realizacaoDt = new DateTime(lateRealizacao);
			lateRealizacao = realizacaoDt.plusDays(1).toDate();
		}
		
		ShowDTO show = new ShowDTO();
		show.setNome(title);
		show.setLocalCaptado(local);
		show.setUrlBase(url);
		show.setDataRealizacao(lateRealizacao);
		show.setUf(uf);
		show.setMunicipio(municipio);
		
		addCapturedDocument(show);
	}
}