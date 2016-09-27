package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class IngressoJaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.evento-informacoes h2").text().trim();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = body.select("div.evento-informacoes h3").text();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String hourStr = body.select("div.evento-informacoes p").html();
		
		if(hourStr == null || hourStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		hourStr = hourStr.split("rio de abertura:</b>")[1].split("h<br>")[0].trim();
		
		dateStr = dateStr.split(",")[1].trim() + " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy' 'HH:mm", new Locale("pt", "BR"));
		
		Date realizacao = dateFormat.parse(dateStr);
		
		String local = body.select("div.evento-informacoes p").html();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = local.split("<br>")[1].split("</b>")[1].trim();
		String uf = null;
		String municipio = null;
		
		if(endereco != null) {
			municipio = StringUtils.clearText(endereco.split(",")[0].toUpperCase().trim());
			uf = endereco.split(",")[1].trim().toUpperCase();
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		local = local.split("<br>")[0].split("</b>")[1].trim();
		
		if(local == null || local.isEmpty()) {
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