package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class BlueTicketNormalizer extends ShowNormalizer {
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout(), "ISO-8859-1", true);
		
		String title = body.select("h1.titulo_interna_evento").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("span.desc_interna_azul").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = body.select("h2.data_interna_evento").text();

		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		try {
			dateStr = dateStr.split(",")[1].trim();
		} catch(Exception e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		String desc = body.select("div.desc_basica_evento").html();

		if(desc == null || desc.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String hourStr = null;
		try {
			hourStr = desc.split("Abertura:<\\/strong>")[1].split("</span>")[0].trim();
		} catch(Exception e) {
			logger.info("Erro no recorte de horário de abertura, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		String uf = null;
		String municipio = null;
		
		if(desc.contains("Cidade/UF</strong>:")) {
			String endereco = desc.split("Cidade\\/UF<\\/strong>:")[1].split("</span>")[0].trim();
			
			uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
			municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		dateStr += " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy' 'HH:mm'h'", new Locale("pt", "BR"));
		
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