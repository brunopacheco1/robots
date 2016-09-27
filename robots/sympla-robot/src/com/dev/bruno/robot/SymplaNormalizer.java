package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringEscapeUtils;
import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class SymplaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("meta[property=og:title]").attr("content");
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("h3#local") == null || body.select("h3#local").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("h3#local").first().parent().select("strong").text();
		
		String [] slices = body.select("h3#local").first().parent().select("p").html().split("<br>");
		
		String endereco = slices[slices.length - 1].toUpperCase();
		
		String municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf(",")).trim());
		
		String uf = endereco.substring(endereco.lastIndexOf(",") + 1).trim();
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		body.select("h3#local").first().parent().select("strong").remove();
		
		String dateStr = body.select("meta[property=og:description]").attr("content");
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.split(",")[1].trim() + " " + dateStr.split(",")[2].trim();
		dateStr = StringEscapeUtils.unescapeHtml4(dateStr.split("Termina")[0].trim());
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy' 'HH'h'mm", new Locale("pt", "BR"));
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy' 'HH'h'", new Locale("pt", "BR"));
			try {
				realizacao = dateFormat.parse(dateStr);
			} catch (ParseException e1) {
				logger.info("Erro no recorte de data, página fora do padrão.");
				setProblemLink(true);
				return;
			}
		}
		
		ShowDTO show = new ShowDTO();
		show.setNome(title);
		show.setLocalCaptado(local);
		show.setUrlBase(url);
		show.setDataRealizacao(realizacao);
		show.setMunicipio(municipio);
		show.setUf(uf);
		
		addCapturedDocument(show);
	}
}