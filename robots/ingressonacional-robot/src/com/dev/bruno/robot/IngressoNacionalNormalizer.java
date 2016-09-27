package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class IngressoNacionalNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.inf-evento h1").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("span.local-evento").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = body.select("span.cidade").text();
		String uf = null;
		String municipio = null;
		
		if(endereco != null && !endereco.isEmpty()) {
			endereco = endereco.split(":")[1];
			municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
			uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		local = local.split(":")[1].trim();
		
		String date = body.select("span.data").text();

		if(date == null || date.isEmpty()) {
			setProblemLink(true);
			return;
		}

		Set<Date> dates = new HashSet<>();
		
		if(!date.contains("-")) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("d' de 'MMMM'/'yyyy' Horário: 'HH':'mm", new Locale("pt", "BR"));
				
				Integer start = Integer.parseInt(date.split("\\s")[1]);
				Integer end = Integer.parseInt(date.split("\\s")[3]);
				
				String suffixDate = date.split("de")[1].trim();
				
				for(;start <= end; start++) {
					
					dates.add(dateFormat.parse(start + " de " + suffixDate));
				}
			} catch(Exception e) {
				logger.info("Erro no recorte de data, página fora do padrão.");
				setProblemLink(true);
				return;
			}
		} else {
			try {
				date = date.split("-")[1].trim();
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("d' de 'MMM'/'yyyy' Horário: 'HH':'mm", new Locale("pt", "BR"));
				
				dates.add(dateFormat.parse(date));
			} catch(Exception e) {
				logger.info("Erro no recorte de data, página fora do padrão.");
				setProblemLink(true);
				return;
			}
		}
		 
		
		for(Date realizacao : dates) {
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