package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

import com.dev.bruno.robot.Normalizing;
import com.dev.bruno.robot.ShowNormalizer;
import com.dev.bruno.utils.GoogleUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class AgendajuNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'HH':'mm");
		
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("meta[property=og:title]").attr("content");
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("p.rsep_location > a[itemprop=name]").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String realizacaoStr = body.select("p.rsep_date").text();
		
		if(realizacaoStr == null || realizacaoStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(realizacaoStr.contains("Início em")) {
			realizacaoStr = realizacaoStr.replaceAll("Início em ", "").replaceAll(" Salvar para calendário", "").trim();
			realizacaoStr = realizacaoStr.split("\\s")[0] + "/" + new DateTime().getYear() + " " + realizacaoStr.split("\\s")[1];
		} else {
			realizacaoStr = realizacaoStr.replaceAll("De ", "").split("até")[0].trim();
			realizacaoStr = realizacaoStr.split("\\s")[0] + "/" + new DateTime().getYear() + " " + realizacaoStr.split("\\s")[1];
		}
		
		Date realizacao = dateFormat.parse(realizacaoStr);
		
		String enderececo = body.select("p.rsep_location > span[itemprop=address]").text();
		String uf = null;
		String municipio = null;
		
		if(enderececo != null && !enderececo.isEmpty()) {
			Map<String, String> resultado = GoogleUtils.findAddress(enderececo);
			
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
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