package com.dev.bruno.robot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Document;

@Normalizing(documentType=DocumentType.SHOW)
public class YupingressosNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument("http://sis.yupingressos.com.br/lojanew/form_compra1.asp?tploja=s&usu_id=&eve_cod=" + url.substring(url.indexOf("evento/") + "evento/".length()).split("\\/")[0], normalizerDTO.getConnectionTimeout());
		
		String title = body.select("div.titulo_dos_eventos_detalhes").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("div.img_local").first().parent().parent().select("td").get(1).html();
		
		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String endereco = local.split("<br>")[1];
		
		String uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
		
		String municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		local = local.split("<br>")[0].trim();
		
		String dateStr = body.select("div.img_calendario").first().parent().parent().select("td").get(1).text();
		
		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.replaceAll("\\D", "");
		
		String hourStr = body.select("div.img_horario").first().parent().parent().select("td").get(1).text();
		
		if(hourStr == null || hourStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr += " " + hourStr.replaceAll("\\D", "");
		
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