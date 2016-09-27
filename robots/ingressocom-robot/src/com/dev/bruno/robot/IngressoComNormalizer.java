package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;

@Normalizing(documentType=DocumentType.SHOW)
public class IngressoComNormalizer extends ShowNormalizer {

	@SuppressWarnings("unchecked")
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Gson gson = new Gson();
		
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("[itemprop=name]").text();

		String espetaculoId = body.select("input#hdnEspetaculo").val();
		
		String cidade = url.split("\\/")[3];
		
		DateTime now = new DateTime();
		
		List<String> dates = new ArrayList<>();
		dates.add(now.toString("yyyyMM"));
		dates.add(now.plusMonths(1).toString("yyyyMM"));
		dates.add(now.plusMonths(2).toString("yyyyMM"));
		dates.add(now.plusMonths(3).toString("yyyyMM"));
		dates.add(now.plusMonths(4).toString("yyyyMM"));
		dates.add(now.plusMonths(5).toString("yyyyMM"));
		
		Boolean found = false;
		
		for(String date : dates) {
			Document hoursBody = documentService.getDocument(String.format("http://www.ingresso.com/%s/home/espetaculo/horarios?espetaculo=%s&grupo=&data=%s", cidade, espetaculoId, date), normalizerDTO.getConnectionTimeout());
			
			if(hoursBody.location().matches("http:\\/\\/www\\.ingresso\\.com\\/erro\\/404")) {
				continue;
			}
			
			List<Map<String, Object>> hours = null;
			
			try {
				hours = gson.fromJson(hoursBody.select("script[type=application/ld+json]").html(), ArrayList.class);
			} catch (Exception e) {
				logger.info("JSON de horas mal formatado. Problema da fonte, nada a ser feito.");
				continue;
			}
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			
			for(Map<String, Object> hour : hours) {
				String startDate = (String) hour.get("startDate");
				
				if(!startDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
					logger.log(Level.SEVERE, "Formato da data mudou!");
					continue;
				}
				
				Map<String, Object> location = (Map<String, Object>) hour.get("location");
				
				Map<String, Object> endereco = (Map<String, Object>) location.get("address");
				
				String municipio = null;
				
				String uf = null;
				
				if(endereco != null) {
					municipio = (String) endereco.get("addressLocality");
					
					if(municipio != null) {
						municipio = StringUtils.clearText(municipio.toUpperCase());
						
						if(municipio.contains(",")) {
							municipio = municipio.substring(municipio.lastIndexOf(",") + 1).trim();
						}
					}
					
					uf = (String) endereco.get("addressRegion");
					
					if(uf != null) {
						uf = StringUtils.clearText(uf.toLowerCase());
						
						uf = ufs.get(uf);
					}
				}
				
				if(uf == null || municipio == null) {
					uf = null;
					municipio = null;
				}
				
				Date realizacao = dateFormat.parse((String) hour.get("startDate"));
				
				ShowDTO show = new ShowDTO();
				show.setNome(title);
				show.setLocalCaptado(location.get("name").toString());
				show.setUrlBase(url);
				show.setDataRealizacao(realizacao);
				show.setUf(uf);
				show.setMunicipio(municipio);
				
				addCapturedDocument(show);
				
				found = true;
			}
		}
		
		if(!found) {
			setProblemLink(true);
		}
	}
}