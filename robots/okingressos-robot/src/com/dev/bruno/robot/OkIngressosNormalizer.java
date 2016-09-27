package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class OkIngressosNormalizer extends ShowNormalizer {
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div#eventInfo > div > h1").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = null;
		String hourStr = null;
		String endereco = null;
		
		for(Element p : body.select("ul.list-group.eventoInfo > li")) {
			String text = p.text();
			
			if(text.toLowerCase().startsWith("horário de início previsto:")) {
				hourStr = text.split("previsto:")[1].trim().replaceAll("\\D", "");
			} else if(text.toLowerCase().startsWith("local:")) {
				local = text.split(":")[1].trim();
			} else if(text.toLowerCase().startsWith("cidade/uf:")) {
				endereco = text.split(":")[1].trim();
			}
		}

		if(local == null || hourStr == null) {
			setProblemLink(true);
			return;
		}
		
		String [] slices = url.split("_");
		
		if(slices.length < 2) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = slices[slices.length - 2];
		
		Map<String, String> resultado = GoogleUtils.findAddress(endereco);
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		dateStr += " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'-'MM'-'yyyy' 'HHmm");
		
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