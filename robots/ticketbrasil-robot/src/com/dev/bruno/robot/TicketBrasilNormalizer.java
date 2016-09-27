package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class TicketBrasilNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		if(body.select(".TabsDetalhes").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		Element details = Jsoup.parse(body.select(".TabsDetalhes").first().html().replace("&nbsp;", ""));
		
		String title = Jsoup.parse(details.html().split("Evento:</b>")[1].split("</li>")[0]).text().trim();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = Jsoup.parse(details.html().split("Data:</b>")[1].split("</li>")[0]).text().trim();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String hourStr = Jsoup.parse(details.html().split("Hora:</b>")[1].split("</li>")[0]).text().trim();
		
		if(hourStr == null || hourStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr + " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'-'MM'-'yyyy' 'HH'h'mm");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			dateFormat = new SimpleDateFormat("dd'-'MM'-'yyyy' 'HH':'mm");
			
			try {
				realizacao = dateFormat.parse(dateStr);
			} catch (ParseException e1) {}
		}
		
		if(realizacao == null) {
			setProblemLink(true);
			return;
		}
		
		String local = Jsoup.parse(details.html().split("Local:</b>")[1].split("</li>")[0]).text().trim();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String municipio = StringUtils.clearText(Jsoup.parse(details.html().split("Cidade:</b>")[1].split("</li>")[0]).text().toUpperCase().trim());
		
		String uf = Jsoup.parse(details.html().split("UF:</b>")[1].split("</li>")[0]).text().toUpperCase().trim();
		
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