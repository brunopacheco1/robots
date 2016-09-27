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

@Normalizing(documentType=DocumentType.SHOW)
public class ShowsCampinasNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h3.post-title").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String desc = Jsoup.parse(body.select("div.post-body > div").html().replaceAll("&nbsp;", "")).text();
		
		if(desc == null || desc.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = desc.split("Local:")[1].split("Endereço:")[0].trim();
		
		String endereco = desc.split("Endereço:")[1].split("(Atrações|Ingressos)")[0].trim();
		
		String uf = null;
		String municipio = null;
		
		if(endereco != null) {
			endereco = endereco.substring(endereco.lastIndexOf("-") + 1).trim();
			uf = endereco.split("/")[1].trim().toUpperCase().replaceAll("(\\.|,)", "");
			municipio = StringUtils.clearText(endereco.split("/")[0].trim().toUpperCase()).replaceAll("(\\.|,)", "");
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}

		String dateStr = desc.split("(Data|Datas):")[1].split("Horá(rios|rio):")[0].replaceAll("\\D", "");
		String hourStr = desc.split("Horá(rios|rio):")[1].split("\\.")[0].replaceAll("\\D", "");
		
		dateStr += " " + hourStr;
		
		if(dateStr.length() != 13) {
			setProblemLink(true);
			return;
		}
		
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