package com.dev.bruno.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Normalizing(documentType=DocumentType.SHOW)
public class FacebookNormalizer extends ShowNormalizer {

	private String accessToken;
	
	public FacebookNormalizer() {
		String credencialsUrl = "https://graph.facebook.com/oauth/access_token?client_id=1113523825333831&client_secret=03e95b72b98a50103bd20872e232efc9&grant_type=client_credentials";
		
    	Client client = ClientBuilder.newClient();
		
		accessToken = client.target(credencialsUrl).request().get(String.class);
		
		accessToken = accessToken.substring(accessToken.indexOf("=") + 1);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(NormalizerDTO normalizerDTO, String url) throws Exception {
		if(accessToken == null) {
			throw new Exception("Falha ao adquirir chave de acesso ao Facebook.");
		}
		
		String eventId = url.split("\\/")[url.split("\\/").length - 1];
		
		String facebookURL = "https://graph.facebook.com/v2.5/" + eventId + "?fields=name,place,start_time&access_token=" + accessToken;
		
		Gson gson = new GsonBuilder().create();
		
		Client client = ClientBuilder.newClient();
		
		Map<String, Object> response = gson.fromJson(client.target(facebookURL).request().get(String.class), HashMap.class);
		
		if(!response.containsKey("name")) {
			setProblemLink(true);
			return;
		}
		
		if(!response.containsKey("place")) {
			setProblemLink(true);
			return;
		}
		
		if(!response.containsKey("start_time")) {
			setProblemLink(true);
			return;
		}
		
		Map<String, Object> place = (Map<String, Object>) response.get("place");
		
		if(!place.containsKey("name")) {
			setProblemLink(true);
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
		
		String title = (String) response.get("name");
		
		String startTimeStr = (String) response.get("start_time");
		
		if(startTimeStr.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-\\d{4}$")) {
			startTimeStr = startTimeStr.substring(0, startTimeStr.lastIndexOf("-"));
		} else if(startTimeStr.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{4}$")) {
			startTimeStr = startTimeStr.substring(0, startTimeStr.lastIndexOf("+"));
		}
		
		Date realizacao = dateFormat.parse(startTimeStr);
		
		String local = (String) place.get("name");
		
		String municipio = null;
		String uf = null;
		
		if(place.containsKey("location")) {
			Map<String, Object> location = (Map<String, Object>) place.get("location");
			
			uf = location.get("state").toString().toUpperCase().trim();
			municipio = StringUtils.clearText(location.get("city").toString().toUpperCase().trim());
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