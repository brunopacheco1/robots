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
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class WayTicketNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String title = body.select("h2.titulo_comprar").text();
		
		if(title == null || title.isEmpty() || !title.contains("|")) {
			setProblemLink(true);
			return;
		}
		
		String dateStr = title.split("\\|")[1].trim();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE', 'd' de 'MMMM' de 'yyyy", new Locale("pt", "BR"));
		
		Date realizacao = null;
		try {
			realizacao = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.info("Erro no recorte de data, página fora do padrão.");
			setProblemLink(true);
			return;
		}
		
		title = title.split("\\|")[0].trim();
		
		String local = null;
		String endereco = null;
		String uf = null;
		String municipio = null;
		
		for(Element li : body.select("ul.info-evento > li")) {
			if(li.text().toLowerCase().contains("cidade")) {
				endereco = li.text().split(":")[1].trim();
			}
		}
		
		if(dateStr == null || dateStr.isEmpty() || endereco == null || endereco.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		local = endereco;
		
		uf = endereco.substring(endereco.lastIndexOf("-") + 1).trim().toUpperCase();
		municipio = StringUtils.clearText(endereco.substring(0, endereco.lastIndexOf("-")).trim().toUpperCase());
		
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