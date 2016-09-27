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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Normalizing(documentType=DocumentType.SHOW)
public class CompreIngressosNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("meta[property=og:title]").attr("content");
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.cont_teatro > p.teatro > a").text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String uf = null;
		String municipio = null;
		
		String endereco = body.select("div.container.g > p.teatro").text();

		if(endereco != null && !endereco.isEmpty()) {
			String [] slices = endereco.split("-");
			
			uf = slices[slices.length - 1].trim().toUpperCase();
			municipio = StringUtils.clearText(slices[slices.length - 2].trim().toUpperCase());
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		Elements dates = body.select("div#iframe a.data");
		
		if(dates.isEmpty()) {
			setProblemLink(true);
			return;
		}

		for(Element date : dates) {
		
			String dateStr = date.select("span.data").text() + " " + new DateTime().getYear() + " " + date.select("span.dia_hora").text().split("\\s")[1].trim();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd' 'MMM' 'yyyy' 'HH'h'mm", new Locale("pt", "BR"));

			Date realizacao = null;
			try {
				realizacao = dateFormat.parse(dateStr);
			} catch (ParseException e) {
				logger.info("Erro no recorte de data, página fora do padrão.");
				setProblemLink(true);
				return;
			}

			if(realizacao.before(new Date())) {
				DateTime realizacaoDt = new DateTime(realizacao);
				realizacao = realizacaoDt.withYear(realizacaoDt.getYear() + 1).toDate();
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
}