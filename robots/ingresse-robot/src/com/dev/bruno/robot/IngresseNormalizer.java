package com.dev.bruno.robot;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

import com.google.gson.Gson;

@Normalizing(documentType=DocumentType.SHOW)
public class IngresseNormalizer extends ShowNormalizer {

	@SuppressWarnings("unchecked")
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'HH':'mm':'00");
		
		Gson gson = new Gson();
		
		String id = url.split("\\?id=")[1].trim();
		
		URL urlEspetaculos = new URL("https://elastic.ingresse.com/events/_search?q=id:" + id + "&sort=date.dateTime.date:asc&size=1&from=0");
		
		URLConnection conn = urlEspetaculos.openConnection();
		conn.setReadTimeout(normalizerDTO.getConnectionTimeout().intValue());
		
		Map<String, Object> resultado = gson.fromJson(IOUtils.toString(conn.getInputStream()), HashMap.class);
		
		if(!resultado.containsKey("hits")) {
			return;
		}
		
		Map<String, Object> hits = (Map<String, Object>) resultado.get("hits");
		
		Double totalResult = (Double) hits.get("total");
		
		if(totalResult != 1d || !hits.containsKey("hits")) {
			return;
		}
		
		List<Map<String, Object>> hitsResult = (List<Map<String, Object>>) hits.get("hits");
		
		for(Map<String, Object> hit : hitsResult) {
			if(!hit.containsKey("_source")) {
				continue;
			}
			
			Map<String, Object> source = (Map<String, Object>) hit.get("_source");
			
			String title = (String) source.get("title");
			
			if(title == null || title.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			List<Map<String, Object>> dates = (List<Map<String, Object>>) source.get("date");
			
			if(dates == null || dates.isEmpty()) {
				setProblemLink(true);
				return;
			}
			
			for(Map<String, Object> date : dates) {
				date = (Map<String, Object>) date.get("dateTime");
				
				if(date == null || date.isEmpty()) {
					setProblemLink(true);
					return;
				}
				
				String dateStr = date.get("date") + " " + date.get("time");
				
				Date realizacao = null;
				try {
					realizacao = dateFormat.parse(dateStr);
				} catch (Exception e) {
					setProblemLink(true);
					return;
				}
				
				Map<String, Object> venue = (Map<String, Object>) source.get("venue");
				
				if(venue == null || venue.isEmpty()) {
					setProblemLink(true);
					return;
				}
				
				String local = (String) venue.get("name");
				
				if(local == null || local.isEmpty()) {
					setProblemLink(true);
					return;
				}
				
				String municipio = null;
				String uf = null;
				
				if(venue.containsKey("city") && venue.containsKey("state")) {
					municipio = StringUtils.clearText(venue.get("city").toString().trim().toUpperCase());
					uf = (String) venue.get("state").toString().trim().toUpperCase();
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
	}
}