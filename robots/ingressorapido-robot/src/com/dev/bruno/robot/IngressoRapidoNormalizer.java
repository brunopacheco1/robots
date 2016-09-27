package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Normalizing(documentType=DocumentType.SHOW)
public class IngressoRapidoNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h1#cphBody_tituloEvento").text();
		
		if(title.isEmpty()) {
			title = body.select("div.information > div.name").text();
		}
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String uf = null;
		String municipio = null;
		
		if(!body.select("span.eventAddress").isEmpty()) {
			String endereco = body.select("span.eventAddress").last().text();
			municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
			uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
		} else {
			municipio = StringUtils.clearText(body.select("span#cphBody_lblCidade").text().toUpperCase());
			uf = body.select("span#cphBody_lblEstado").text().toUpperCase();
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}

		String local = body.select("span#cphBody_lblLocal").text();

		if(local.isEmpty()) {
			Elements elements = body.select("div.information > div.details > span");
			local = elements.get(elements.size() - 3).text();
		}
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMMM/yyyy'T'HH:mm", new Locale("pt", "BR"));
		
		Boolean found = false;
		
		for(Element dataOption : body.select("select#cphBody_cmbApresentacao > option")) {
			
			String dataStr = null;
			
			if(dataOption.text().contains("Selecione aqui a Data e Hora")) {
				continue;
			}
			
			dataStr = dataOption.text().split(",")[1].trim().replaceAll("\\sde\\s", "/");

			dataStr = dataStr.split("\\s")[0] + "T" + dataStr.split("\\s")[2];

			Date realizacao = dateFormat.parse(dataStr);
			
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
			
			found = true;
		}

		for(Element dataOption : body.select("span.eventLastDate")) {
			SimpleDateFormat newDateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy'/'HH'h'mm", new Locale("pt", "BR"));
			
			String [] horaSplit = dataOption.parent().select("span").get(0).text().split("\\s");
			String hora = horaSplit[horaSplit.length - 1].trim();
			String data = dataOption.text().replaceAll("a\\s", "") + " de " + new DateTime().getYear() + "/" + hora;
			
			DateTime realizacao = new DateTime(newDateFormat.parse(data));
			
			if(realizacao.isBeforeNow()) {
				DateTime realizacaoDt = new DateTime(realizacao);
				realizacao = realizacaoDt.withYear(realizacaoDt.getYear() + 1);
			}
			
			ShowDTO show = new ShowDTO();
			show.setNome(title);
			show.setLocalCaptado(local);
			show.setUrlBase(url);
			show.setDataRealizacao(realizacao.toDate());
			show.setUf(uf);
			show.setMunicipio(municipio);
			
			addCapturedDocument(show);
			
			found = true;
		}
		
		if(!found) {
			setProblemLink(true);
		}
	}
}