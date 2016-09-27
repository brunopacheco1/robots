package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import com.dev.bruno.utils.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class EuAgitoNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		if(body.select("div.container h1").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String title = body.select("div.container h1").first().text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		if(body.select("div.container blockquote > p").size() != 2) {
			setProblemLink(true);
			return;
		}

		String dateStr = body.select("div.container blockquote > p").get(0).text();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd' de 'MMMM' de 'yyyy");
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr + " de " + new DateTime().getYear());
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		if(realizacao.before(new Date())) {
			realizacao = dateFormat.parse(dateStr + " de " + (new DateTime().getYear() + 1));
		}
		
		String local = null;
		String uf = null;
		String municipio = null;
		
		String endereco = body.select("div.container blockquote > p").get(1).text();
		
		if(endereco.matches("^.+\\s-\\s.+\\/\\w{2}$")) {
			uf = endereco.substring(endereco.lastIndexOf("/") + 1).trim().toUpperCase();
			municipio = StringUtils.clearText(endereco.substring(endereco.lastIndexOf("-") + 1, endereco.lastIndexOf("/")).trim().toUpperCase());
			local = endereco.substring(0, endereco.lastIndexOf("-")).trim();
		} else {
			local = endereco;
			
			Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
			
			uf = resultado.get("uf");
			municipio = resultado.get("municipio");
		}
		
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