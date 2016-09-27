package com.dev.bruno.robot;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class BilheteriaVirtualNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		String data = url.split("\\?")[1];
		
		String title = URLDecoder.decode(data.split("&")[0].split("=")[1], "UTF-8");
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = URLDecoder.decode(data.split("&")[1].split("=")[1], "UTF-8");

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = local.split("\\|")[1].trim();
		
		local = local.split("\\|")[0].trim();
		
		String municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
		String uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase().substring(0, 2);
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String date = URLDecoder.decode(data.split("&")[2].split("=")[1], "UTF-8");

		if(date == null || date.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String hour = URLDecoder.decode(data.split("&")[3].split("=")[1], "UTF-8");

		if(hour == null || hour.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		hour = hour.replaceAll("\\D", "");
		
		date += " " + hour;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'/'M'/'yyyy' 'HHmm");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(date);
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