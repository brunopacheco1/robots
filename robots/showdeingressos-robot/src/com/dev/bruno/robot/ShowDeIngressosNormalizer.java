package com.dev.bruno.robot;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class ShowDeIngressosNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String data = url.split("\\?")[1];
		
		String title = URLDecoder.decode(data.split("&")[0].split("=")[1], "UTF-8");
		
		String dateStr = URLDecoder.decode(data.split("&")[1].split("=")[1], "UTF-8");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MM'/'yyyy");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		String endereco = URLDecoder.decode(data.split("&")[2].split("=")[1], "UTF-8");
		
		String uf = endereco.substring(endereco.lastIndexOf("/") + 1).trim().toUpperCase();
		String municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("/")).trim().toUpperCase());
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String local = body.select("div.local-endereco > h2").first().text();
		
		if(local == null || local.isEmpty()) {
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