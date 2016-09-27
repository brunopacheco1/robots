package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class EntravipNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1.evento_nome").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("li#p_descricao li").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = null;
		String local = null;
		String hourStr = null;
		String uf = null;
		String municipio = null;
		
		for(Element strong : body.select("li#p_descricao li")) {
			if(strong.text().toLowerCase().contains("data")) {
				dateStr = Jsoup.parse(strong.html().replaceAll("&nbsp;", "")).text().split(":")[1].trim();
			} else  if(strong.text().toLowerCase().contains("hora")) {
				hourStr = Jsoup.parse(strong.html().replaceAll("&nbsp;", "")).text().split(":")[1].trim();
			} else if(strong.text().toLowerCase().contains("local")) {
				local = Jsoup.parse(strong.html().replaceAll("&nbsp;", "")).text().split(":")[1].trim();
			} else if(strong.text().toLowerCase().contains("cidade")) {
				municipio = StringUtils.clearText(Jsoup.parse(strong.html().replaceAll("&nbsp;", "")).text().split(":")[1].toUpperCase().trim());
			} else if(strong.text().toLowerCase().contains("estado")) {
				uf = Jsoup.parse(strong.html().replaceAll("&nbsp;", "")).text().split(":")[1].trim();
			}
		}
		
		if(dateStr == null || local == null || hourStr == null) {
			setProblemLink(true);
			return;
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		dateStr = dateStr.replaceAll("\\D", "");
		
		hourStr = hourStr.replaceAll("\\D", "");
		
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