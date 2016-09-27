package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class GuiaDaBoaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = null;
		String dateStr = null;
		String hourStr = "0000";
		String uf = null;
		String municipio = null;
		String endereco = null;
		
		for(Element div : body.select("div#details > div")) {
			String text = div.text();
			if(div.text().toLowerCase().contains("data")) {
				dateStr = text.substring(text.indexOf(":") + 1).trim().replaceAll("\\D", "");
			} else  if(div.text().toLowerCase().contains("início")) {
				hourStr = text.substring(text.indexOf(":") + 1).trim().replaceAll("\\D", "");
			} else  if(div.text().toLowerCase().contains("horário")) {
				hourStr = text.substring(text.indexOf(":") + 1).trim().replaceAll("\\D", "");
			} else  if(div.text().toLowerCase().contains("a partir das")) {
				hourStr = text.substring(text.indexOf(":") + 1).trim().replaceAll("\\D", "");
			} else if(div.text().toLowerCase().contains("local do evento")) {
				local = text.substring(text.indexOf(":") + 1).trim();
			} else if(div.text().toLowerCase().contains("estado")) {
				uf = StringUtils.clearText(text.substring(text.indexOf(":") + 1).trim().toLowerCase());
			} else if(div.text().toLowerCase().contains("cidade")) {
				municipio = StringUtils.clearText(text.substring(text.indexOf(":") + 1).trim().toUpperCase());
			} else if(div.text().toLowerCase().contains("endereço")) {
				endereco = text.substring(text.indexOf(":") + 1).trim();
			}
		}
		
		if(dateStr == null || local == null || hourStr == null) {
			setProblemLink(true);
			return;
		}
		
		if((uf == null || municipio == null) && endereco != null) {
			Map<String, String> resultado = GoogleUtils.findAddress(endereco);
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
		if(uf == null || municipio == null) {
			Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
		uf = ufs.get(StringUtils.clearText(uf));
		
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