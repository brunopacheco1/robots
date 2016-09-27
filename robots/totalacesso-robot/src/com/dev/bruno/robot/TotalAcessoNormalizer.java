package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class TotalAcessoNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Map<String, String> months = new HashMap<>();
		
		months.put("janeiro", "01");
		months.put("fevereiro", "02");
		months.put("março", "03");
		months.put("abril", "04");
		months.put("maio", "05");
		months.put("junho", "06");
		months.put("julho", "07");
		months.put("agosto", "08");
		months.put("setembro", "09");
		months.put("outubro", "10");
		months.put("novembro", "11");
		months.put("dezembro", "12");
		
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("span.EventName").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("strong.information").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String desc = body.select("strong.information").first().parent().text();
		
		String local = null;
		String uf = null;
		String municipio = null;
		
		try {
			local = desc.split("Local:")[1].split("Horário:")[0].trim();
			
			String endereco = desc.split("Cidade:")[1].split("Local:")[0].trim();
			
			uf = endereco.split("/")[1].replaceAll("(\\.|,)", "");
			municipio = StringUtils.clearText(endereco.split("/")[0].toUpperCase());
		} catch (Exception e) {
			setProblemLink(true);
			return;
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		Set<String> dates = new HashSet<>();
		
		for(Element dateEl : body.select("li.Selling")) {
			String dateStr = dateEl.text();
			
			String newDateStr = "";
			
			for(String slice : dateStr.split("\\s")) {
				slice = slice.replaceAll("\\.", "");
				slice = slice.replaceAll("\\,", "").toLowerCase();
				
				if(months.containsKey(slice)) {
					newDateStr += months.get(slice);
				} else {
					newDateStr += slice;
				}
			}
			
			dates.add(newDateStr.replaceAll("\\D", ""));
		}
		
		for(String newDateStr : dates) {
			if(newDateStr.length() != 12) {
				setProblemLink(true);
				break;
			}
			
			newDateStr = newDateStr.substring(0, 8) + " " + newDateStr.substring(8);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy' 'HHmm");
			
			Date realizacao = null;
			try {
				realizacao = dateFormat.parse(newDateStr);
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
}