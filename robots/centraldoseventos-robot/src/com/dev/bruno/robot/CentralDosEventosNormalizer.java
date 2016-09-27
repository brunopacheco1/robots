package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class CentralDosEventosNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		if(body.select("div#meio-01-banner > span").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String title = body.select("div#meio-01-banner > span").first().text();
		
		String dateStr = title.substring(title.indexOf("|") + 1).trim();
		title = title.substring(0, title.indexOf("|")).trim();
		
		dateStr = dateStr.split(",")[1];
		
		if(body.select("ul.info-evento > li").size() != 2) {
			setProblemLink(true);
			return;
		}
		
		String uf = body.select("ul.info-evento > li").first().text().toUpperCase().split(":")[1];
		String municipio = StringUtils.clearText(uf.substring(0, uf.lastIndexOf("-")).trim());
		uf = uf.substring(uf.lastIndexOf("-") + 1).trim();
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String endereco = body.select("ul.info-evento > li").last().html();
		
		if(!endereco.contains("Local:")) {
			setProblemLink(true);
			return;
		}
		
		String hourStr = "00H00";
		String local = null;
		
		if(endereco.contains("Horário:")) {
			hourStr = endereco.split("Horário:")[1].split("<br>")[0].trim();
			local = endereco.split("Local:")[1].split("<br>")[0].trim();
		} else {
			local = endereco.split("Local:")[1].split("<br>")[0].trim();
		}
		
		dateStr += " " + hourStr;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy' 'HH'H'mm", new Locale("pt", "BR"));
		
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