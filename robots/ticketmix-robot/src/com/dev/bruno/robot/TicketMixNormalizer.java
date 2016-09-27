package com.dev.bruno.robot;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.joda.time.DateTime;

@Normalizing(documentType=DocumentType.SHOW)
public class TicketMixNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Map<String, String> months = new HashMap<>();
		
		months.put("jan", "01");
		months.put("fev", "02");
		months.put("mar", "03");
		months.put("abr", "04");
		months.put("mai", "05");
		months.put("jun", "06");
		months.put("jul", "07");
		months.put("ago", "08");
		months.put("set", "09");
		months.put("out", "10");
		months.put("nov", "11");
		months.put("dez", "12");
		
		String data = url.split("\\?")[1];
		
		String title = URLDecoder.decode(data.split("title=")[1].split("&")[0], "UTF-8");

		String dateStr = URLDecoder.decode(data.split("date=")[1].split("&")[0], "UTF-8");
		
		dateStr = dateStr.replaceAll("<.*?>", "").split(",")[1];
		
		String newDateStr = "";
		
		for(String slice : dateStr.split("\\s")) {
			slice = slice.replaceAll("\\.", "");
			slice = slice.replaceAll("\\,", "").toLowerCase();
			
			if(months.containsKey(slice)) {
				newDateStr += months.get(slice);
			} else if(slice.matches("^\\d+$") && slice.length() == 1) {
				newDateStr += "0" + slice;
			} else {
				newDateStr += slice.replaceAll("\\D", "");
			}
		}
		
		String hourStr = newDateStr.substring(4);
		
		if(hourStr.length() == 2) {
			hourStr += "00";
		}
		
		dateStr = newDateStr.substring(0, 4) + new DateTime().getYear() + " " + hourStr;
		
		String local = URLDecoder.decode(data.split("location=")[1].split("&")[0], "UTF-8");
		
		Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
		
		String uf = resultado.get("uf");
		String municipio = resultado.get("municipio");
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
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
		
		if(realizacao.before(new Date())) {
			dateStr = newDateStr.substring(0, 4) + (new DateTime().getYear() + 1) + " " + hourStr;
			realizacao = dateFormat.parse(dateStr);
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