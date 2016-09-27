package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;
import org.jsoup.nodes.Element;

@Normalizing(documentType=DocumentType.SHOW)
public class IngressoCertoNormalizer extends ShowNormalizer {

	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		org.jsoup.nodes.Document body = documentService.getDocument(url, normalizerDTO.getConnectionTimeout());
		
		String tipo = null;
		
		String municipio = null;
		
		String uf = null;
		
		for(Element element : body.select("span.name-metatado > b")) {
			if(element.text().contains("Tipo:")) {
				Element parent = element.parent();
				parent.select("b").remove();
				tipo = parent.text().toLowerCase().trim();
			}
			
			if(element.text().contains("Cidade:")) {
				Element parent = element.parent();
				parent.select("b").remove();
				municipio = StringUtils.clearText(parent.text().toUpperCase().trim());
			}
			
			if(element.text().contains("Estado:")) {
				Element parent = element.parent();
				parent.select("b").remove();
				uf = StringUtils.clearText(parent.text().toLowerCase().trim());
			}
		}
		
		if(tipo != null && tipo.equals("esportes")) {
			setProblemLink(true);
			return;
		}
		
		uf = ufs.get(uf);
		
		if(uf == null || municipio == null) {
			uf = null;
			municipio = null;
		}
		
		String title = body.select("div.information > div > h1").text();
		
		if(title == null || title.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		
		if(body.select("span.localevento").isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		String local = body.select("span.localevento").first().text();

		if(local == null || local.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		local = local.replaceAll("Local do evento:\\s", "").split("-")[0].trim();
		
		String dateStr = body.select("span.dataevento").first().text();

		if(dateStr == null || dateStr.isEmpty()) {
			setProblemLink(true);
			return;
		}
		
		dateStr = dateStr.replaceAll("Data:\\s", "").trim();
		
		String hourStr = "00h";
		
		if(!body.select("span.horario_abertura").isEmpty()) {
			hourStr = body.select("span.horario_abertura").first().text();
	
			if(hourStr == null || hourStr.isEmpty()) {
				setProblemLink(true);
				return;
			}
		}
		
		hourStr = hourStr.replaceAll("Horário\\sde\\sAbertura:\\s", "").trim();
		
		if(hourStr.matches("^\\d{2}$")) {
			hourStr += "h";
		}
		
		dateStr += " " + hourStr;
		
		if(dateStr.contains("Agendar")) {
			setProblemLink(true);
			return;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy' 'HH'h'", new Locale("pt", "BR"));
		
		Date realizacao = dateFormat.parse(dateStr);
		
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