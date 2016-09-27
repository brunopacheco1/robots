package com.dev.bruno.robot;

import java.net.URLDecoder;
import java.util.Map;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.GoogleUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class TudusNormalizer extends ShowNormalizer {

	private static final long TICKS_AT_EPOCH = 621355968000000000L;
    private static final long TICKS_PER_MILLISECOND = 10000;
	
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String data = url.split("\\?")[1];
		
		String title = URLDecoder.decode(data.split("&")[0],"UTF-8");
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		title = title.replace("title=", "");
		
		String local = URLDecoder.decode(data.split("&")[1],"UTF-8");

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		local = local.replace("local=", "");
		
		String municipio = null;
		String uf = null;
		
		Map<String, String> resultado = GoogleUtils.findAddressByPlace(local);
		
		uf = resultado.get("uf");
		municipio = resultado.get("municipio");
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		for(Element checkbox : body.select("div.schedule-content.schedule-time input")) {
			String date = checkbox.attr("data-startdate");
			
			DateTime realizacao = new DateTime((Long.parseLong(date) - TICKS_AT_EPOCH) / TICKS_PER_MILLISECOND);
			realizacao = realizacao.plusHours(3);
			
			ShowDTO show = new ShowDTO();
			show.setNome(title);
			show.setLocalCaptado(local);
			show.setUrlBase(url);
			show.setDataRealizacao(realizacao.toDate());
			show.setUf(uf);
			show.setMunicipio(municipio);
			
			addCapturedDocument(show);
		}
	}
}