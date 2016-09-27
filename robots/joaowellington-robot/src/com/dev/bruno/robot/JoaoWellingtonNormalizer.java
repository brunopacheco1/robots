package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Normalizing(documentType=DocumentType.SHOW)
public class JoaoWellingtonNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("span.tnoticias").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("span.laranja").size() != 4) {
			setProblemLink(true);
			return;
		}
		
		Elements elements = body.select("span.laranja");
		
		String local = elements.get(3).text();
		String dateStr = elements.get(0).text().replaceAll("\\D", "") + " " + elements.get(1).text().replaceAll("\\D", "");
		String uf = null;
		String municipio = elements.get(2).text();
		
		Map<String, String> resultado = GoogleUtils.findAddress(municipio);
		
		uf = resultado.get("uf");
		municipio = resultado.get("municipio");
		
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
			try {
				dateFormat = new SimpleDateFormat("ddMMyyyy' 'HH");
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
		show.setUf(uf);
		show.setMunicipio(municipio);
		
		addCapturedDocument(show);
	}
}