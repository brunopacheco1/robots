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
public class BillboardNormalizer extends ShowNormalizer {
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.desc > h4").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("div.data").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = url.substring(url.lastIndexOf("mes=") + 4) + "-" + body.select("div.data").last().select("em").text().replaceAll("\\s", "");

		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = null;
		String hourStr = null;
		String endereco = null;
		
		for(Element p : body.select("div.desc > p")) {
			String text = p.text();
			
			if(text.toLowerCase().startsWith("horário:")) {
				hourStr = text.split(":")[1].trim();
			} else if(text.toLowerCase().startsWith("local:")) {
				local = text.split(":")[1].trim();
			} else if(text.toLowerCase().startsWith("endereço:")) {
				endereco = text.split(":")[1].trim();
			}
		}

		if(local == null || hourStr == null) {
			setProblemLink(true);
			return;
		}
		
		Map<String, String> resultado = GoogleUtils.findAddress(endereco);
		if(resultado.isEmpty()) {
			resultado = GoogleUtils.findAddressByPlace(local);
		}
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		dateStr += " " + hourStr.replaceAll("\\D", "");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH");
		if(dateStr.matches("^\\d{2}-\\d{2}-\\d{4}\\s\\d{4}$")) {
			dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HHmm");
		}
		
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