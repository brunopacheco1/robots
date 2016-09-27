package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Normalizing(documentType=DocumentType.SHOW)
public class EccopassNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		Elements elements = body.select("div.panel-body h4");
		
		if(elements.size() != 3) {
			setProblemLink(true);
			return;
		}
		
		String title = elements.get(0).text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = elements.get(2).text();
		
		String local = endereco.substring(0, endereco.indexOf("-")).trim();
		endereco = endereco.substring(endereco.indexOf("-") + 1).trim();
		
		String uf = endereco.split("\\/")[1].trim().toUpperCase();
		String municipio = StringUtils.clearText(endereco.split("\\/")[0].toUpperCase().trim());
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String dataStr = body.select("div.panel-body h5").text();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' - 'HH':'mm");
			
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dataStr);
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