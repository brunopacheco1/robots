package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import org.jsoup.nodes.Document;

import com.dev.bruno.robot.Normalizing;
import com.dev.bruno.robot.ShowNormalizer;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class AgitouUberlandiaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm");
		
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1 > span.summary").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.local > h2 > a > span[itemprop=name]").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String realizacaoStr = body.select("span.dtstart > span.value-title").attr("title");
		
		if(realizacaoStr == null || realizacaoStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		realizacaoStr = realizacaoStr.substring(0, realizacaoStr.lastIndexOf("-")).trim();
		
		Date realizacao = dateFormat.parse(realizacaoStr);
		
		String uf = body.select("h3.adr > span.region").text();
		
		String municipio = body.select("h3.adr > span.locality").text();
		
		if(municipio != null) {
			municipio = StringUtils.clearText(municipio).toUpperCase().trim();
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
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