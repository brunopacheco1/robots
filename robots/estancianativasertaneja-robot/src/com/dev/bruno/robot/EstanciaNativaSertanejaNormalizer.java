package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class EstanciaNativaSertanejaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("header.page-header > h1").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = body.select("h2.post-date").text();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = null;
		String hourStr = null;
		String uf = null;
		String municipio = null;
		
		for(Element div : body.select("div.post-meta-table > div.row")) {
			if(div.text().toLowerCase().contains("hora")) {
				hourStr = div.text().substring(div.text().indexOf(" ") + 1);
			} else if(div.text().toLowerCase().contains("endere")) {
				local = div.text().substring(div.text().indexOf(" ") + 1);
			} else if(div.text().toLowerCase().contains("localiza")) {
				String endereco = div.text().substring(div.text().indexOf(" ") + 1);
				uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
				municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
			}
		}
		
		if(local == null || hourStr == null) {
			setProblemLink(true);
			return;
		}
		
		dateStr += " " + hourStr;
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'HH'h'mm");
		
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