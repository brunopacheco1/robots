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
public class SouESNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.content > h1.subtitle").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = null;
		String endereco = null;
		String dateStr = null;
		String hourStr = null;
		
		for(Element td : body.select("div#informacoes td")) {
			String text = td.text();
			if(text.toLowerCase().contains("cidade") || text.toLowerCase().contains("data") || text.toLowerCase().contains("horário")) {
				String [] slices = td.html().replaceAll("<br><br>", "<br>").split("<br>");
				
				for (int i = 0; i < slices.length; i++) {
					String slice = slices[i].trim();
					
					if(slice.contains("Local")) {
						local = slices[i + 1];
					} else if(slice.contains("Endereço")) {
						endereco = slices[i + 1];
					} else if(slice.contains("Data")) {
						dateStr = slices[i + 1];
					} else if(slice.contains("Horário")) {
						hourStr = slices[i + 1];
					}
				}
			}
		}
		
		if(local == null || local.isEmpty() || endereco == null || endereco.isEmpty() || dateStr == null || dateStr.isEmpty() || hourStr == null || hourStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		hourStr = hourStr.replaceAll("\\D", "");
		
		if(hourStr.length() == 2) {
			hourStr += "00";
		}
		
		dateStr += hourStr;
		
		dateStr = dateStr.replaceAll("\\D", "");
		
		dateStr = dateStr.substring(0,8) + " " + dateStr.substring(8, 12);
		
		Map<String, String> resultado = GoogleUtils.findAddress(endereco);
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
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