package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class MinhaEntradaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.box-info-event h3").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.event-address h4").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String date = null;
		
		String hour = null;
		
		String endereco = null;
		
		String atracaoPrincipal = null;
		
		String municipio = null;
		
		String uf = null;
		
		for(Element tr : body.select("div.box-info-event tr")) {
			if(tr.text().toLowerCase().contains("data")) {
				date = tr.text();
			}
			
			if(tr.text().toLowerCase().contains("horário")) {
				hour = tr.text();
			}
			
			if(tr.text().toLowerCase().contains("atração principal")) {
				atracaoPrincipal = tr.text();
			}
			
			if(tr.text().toLowerCase().contains("cidade")) {
				endereco = tr.text();
			}
		}
		
		if(date == null || hour == null) {
			setProblemLink(true);
			return;
		}
		
		if(atracaoPrincipal == null) {
			atracaoPrincipal = hour;
		}
		
		if(endereco != null && !endereco.isEmpty()) {
			endereco =  endereco.split(":")[1];
			
			uf = endereco.split("/")[1].trim().toUpperCase();
			municipio = StringUtils.clearText(endereco.split("/")[0].trim().toUpperCase());
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		date = date.split(",")[1].trim() + " de " + new DateTime().getYear();
		
		hour = hour.replaceAll("\\D", "");
		atracaoPrincipal = atracaoPrincipal.replaceAll("\\D", "");
		
		String earlyDate = date + " " + hour;
		
		String lateDate = date + " " + atracaoPrincipal;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy' 'HHmm", new Locale("pt", "BR"));
		
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