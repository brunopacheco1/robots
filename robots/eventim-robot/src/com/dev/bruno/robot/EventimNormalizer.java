package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

@Normalizing(documentType=DocumentType.SHOW)
public class EventimNormalizer extends ShowNormalizer {
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		for(Element script : body.select("table.selectionList.results > tbody > tr script")) {
			String json = script.html();
			
			Map<String, Object> objeto = new Gson().fromJson(json, HashMap.class);
			
			String title = (String) objeto.get("name");
			
			if(title == null || title.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			String dateStr = (String) objeto.get("startDate");
			
			if(dateStr == null || dateStr.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			dateStr = dateStr.substring(0, dateStr.lastIndexOf("."));
			
			Map<String, Object> localObj = (Map<String, Object>) objeto.get("location");
			
			String local = (String) localObj.get("name");
			
			if(local == null || local.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			Map<String, Object> endereco = (Map<String, Object>) localObj.get("address");
			
			String cep = (String) endereco.get("postalCode");
			
			String uf = null;
			String municipio = null;
			
			if(cep != null) {
				Map<String, String> resultado = GoogleUtils.findAddress(cep);
				
				uf = resultado.get("uf");
				municipio = resultado.get("municipio");
			}
			
			uf = ufs.get(uf);
			
			if(uf == null || municipio == null) {
				uf = null;
				municipio = null;
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
			
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
}