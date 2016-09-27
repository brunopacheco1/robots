package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class G1Normalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {

		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("[itemprop=name]").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("[itemprop=location] > strong").text();
		
		String endereco = body.select("p.estado").text();
		String uf = null;
		String municipio = null;
		
		if(endereco != null && !endereco.isEmpty()) {
			uf = endereco.split(",")[1].toUpperCase().trim();
			municipio = StringUtils.clearText(endereco.split(",")[0].toUpperCase().trim());
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = body.select("[itemprop=startDate]").attr("datetime");
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
		
		Date realizacao = dateFormat.parse(dateStr);

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