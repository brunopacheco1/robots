package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class BoaDiversaoNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.evento > h2.titulo-principal").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		
		String local = body.select("div.evento > p.data").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}

		Date realizacao = null;
		
		try {
			String dataStr = local.split("\\|")[0].trim();
			String hourStr = dataStr.split("às")[1].trim();
		
			dataStr = dataStr.substring(dataStr.indexOf("-") + 1, dataStr.indexOf("às")).trim() + "/" + new DateTime().getYear() + " " + hourStr;
		
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy' 'HH'h'mm");
			
			realizacao = dateFormat.parse(dataStr);
		} catch (Exception e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		local = local.split("\\|")[1].trim();
		
		Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
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