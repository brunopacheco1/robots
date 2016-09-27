package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import org.jsoup.nodes.Document;

import com.dev.bruno.utils.GoogleUtils;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class AloIngressosNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String desc = body.select("td.value-field.RELEASE").text();
		
		if(desc == null || desc.isEmpty()) {
			setProblemLink(true);
			return;
		}

		if(!desc.contains("Nome do Evento:")) {
			setProblemLink(true);
			return;
		}
		
		if(!desc.contains("Data:")) {
			setProblemLink(true);
			return;
		}
		
		if(!desc.contains("Horário:")) {
			setProblemLink(true);
			return;
		}
		
		if(!desc.contains("Local:")) {
			setProblemLink(true);
			return;
		}
		
		if(!desc.contains("Classificação Mínima")) {
			setProblemLink(true);
			return;
		}
		
		String title = desc.substring(desc.indexOf("Nome do Evento:"), desc.indexOf("Data:")).split(":")[1].trim();
		String dateStr = desc.substring(desc.indexOf("Data:"), desc.indexOf("Horário:")).split(":")[1].replaceAll("\\D", "");
		String hourStr = desc.substring(desc.indexOf("Horário:"), desc.indexOf("Local:")).split(":")[1].replaceAll("\\D", "");
		
		String local = null;
		
		try {
			local = desc.substring(desc.indexOf("Local:"), desc.indexOf("Classificação Mínima")).split(":")[1].trim();
		} catch (Exception e) {
			try {
				local = desc.substring(desc.indexOf("Local:"), desc.indexOf("Ingressos:")).split(":")[1].trim();
			} catch (Exception e1) {
				local = null;
			}
		}
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String uf = null;
		String municipio = null; 
		try {
			uf = local.substring(local.lastIndexOf("-") + 1).toUpperCase().trim();
			municipio = StringUtils.clearText(local.substring(local.indexOf("-") + 1, local.lastIndexOf("-")).toUpperCase().trim());
		} catch (Exception e) {
			Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
			
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		dateStr += " " + hourStr;
		
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