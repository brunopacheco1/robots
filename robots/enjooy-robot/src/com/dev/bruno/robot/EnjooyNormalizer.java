package com.dev.bruno.robot;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class EnjooyNormalizer extends ShowNormalizer {

	private SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm");
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1.evName").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("h2.evPlaceName").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String realizacaoStr = body.select("div.data[itemprop=startDate]").attr("content");
		
		if(realizacaoStr == null || realizacaoStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		Date realizacao = format.parse(realizacaoStr);
		
		String municipio = URLDecoder.decode(url.split("\\?")[1].split("\\&")[0].split("municipio=")[1], "UTF-8");
		
		String uf = URLDecoder.decode(url.split("\\?")[1].split("\\&")[1].split("uf=")[1], "UTF-8");
		
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