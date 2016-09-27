package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class PortalDoIngressoNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("span#ContentPlaceHolder1_lbTituloEvento").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("span#ContentPlaceHolder1_lbLocal").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = body.select("span#ContentPlaceHolder1_lbCidade").text();
		String municipio = null;
		String uf = null;
		
		if(endereco != null && !endereco.isEmpty()) {
			municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
			uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String dateStr = body.select("span#ContentPlaceHolder1_lbDataHoraTitulo").text();

		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("'Dia 'dd'/'MM'/'yyyy' A partir das 'HH'hs'");
		
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