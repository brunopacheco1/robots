package com.dev.bruno.robot;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;

@Normalizing(documentType=DocumentType.SHOW)
public class PidaNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		String data = url.split("\\?")[1];
		
		String title = URLDecoder.decode(data.split("&")[0].split("=")[1], "UTF-8");
		
		String local = URLDecoder.decode(data.split("&")[1].split("=")[1], "UTF-8");
		
		String dateStr = url.split("\\?")[0].substring(url.split("\\?")[0].length() - 10);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd'-'MM'-'yyyy");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
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