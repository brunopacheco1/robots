package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class BilheteriaDigitalNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.tit-cal > h3").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.tit-cal > p.sub-cal").text();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String [] slices = local.split("-");
		
		String hourStr = slices[0].trim();
		String uf = slices[slices.length - 1].trim().toUpperCase();
		String municipio = StringUtils.clearText(slices[slices.length - 2].trim()).toUpperCase();
		local = slices[1].trim();
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String dateStr = body.select("div.calendario").text();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.split("\\s")[0] + "/" + new DateTime().getYear() + " "  + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'MMM'/'yyyy' 'HH':'mm", new Locale("pt", "BR"));
		
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