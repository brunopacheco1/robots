package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class IngressoMaisNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("span#ContentPlaceHolder1_lbTituloEvento").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = body.select("span#ContentPlaceHolder1_lbCidade").text();
		String uf = null;
		String municipio = null;
		
		if(endereco != null) {
			uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
			municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String local = body.select("span#ContentPlaceHolder1_lbLocal").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = body.select("span#ContentPlaceHolder1_lbDataHoraTitulo").text();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.replaceAll("\\D", "");
		
		if(dateStr.length() != 12) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.substring(0, 8) + " " + dateStr.substring(8, 12);
			
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