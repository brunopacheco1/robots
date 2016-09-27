package com.dev.bruno.utils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GoogleUtils {

	@SuppressWarnings("unchecked")
	public static Map<String, String> findAddress(String address) throws Exception {
		if(address == null || address.trim().isEmpty()) {
			return new HashMap<>();
		}
		
    	Gson gson = new GsonBuilder().create();
    	
    	String addressURL = "https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyCmbN_CNrlEr_k91R85snsHip9GXS-l39s&address=" + URLEncoder.encode(address, "UTF-8");
    	
    	Client client = ClientBuilder.newClient();
		
		Map<String, Object> resultado = gson.fromJson(client.target(addressURL).request().get(String.class), HashMap.class);
		
		String responseStatus = (String) resultado.get("status");
		
		if(responseStatus != null && responseStatus.equals("OK")) {
			List<Map<String, Object>> lista = (List<Map<String, Object>>) resultado.get("results");
			
			if(lista.size() == 1) {
				Map<String, Object> enderecoEncontrado = lista.get(0);
				
				return buildAddress(enderecoEncontrado);
			}
		}
		
		return new HashMap<>();
    }
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> findAddressByLatlng(String lat, String lng) throws Exception {
		if(lat == null || lng == null || lat.trim().isEmpty() || lng.trim().isEmpty()) {
			return new HashMap<>();
		}
		
    	Gson gson = new GsonBuilder().create();
    	
    	String addressURL = "https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyCmbN_CNrlEr_k91R85snsHip9GXS-l39s&latlng=" + URLEncoder.encode(lat, "UTF-8") + "," + URLEncoder.encode(lng, "UTF-8");
    	
    	Client client = ClientBuilder.newClient();
		
		Map<String, Object> resultado = gson.fromJson(client.target(addressURL).request().get(String.class), HashMap.class);
		
		String responseStatus = (String) resultado.get("status");
		
		if(responseStatus != null && responseStatus.equals("OK")) {
			List<Map<String, Object>> lista = (List<Map<String, Object>>) resultado.get("results");
			
			if(!lista.isEmpty()) {
				Map<String, Object> enderecoEncontrado = lista.get(0);
				
				return buildAddress(enderecoEncontrado);
			}
		}
		
		return new HashMap<>();
    }
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> findAddressByPlace(String place) throws Exception {
		if(place == null || place.trim().isEmpty()) {
			return new HashMap<>();
		}
		
    	Gson gson = new GsonBuilder().create();
    	
    	String placeURL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?key=AIzaSyCmbN_CNrlEr_k91R85snsHip9GXS-l39s&components=country:br&input=" + URLEncoder.encode(place, "UTF-8");
    	
    	Client client = ClientBuilder.newClient();
		
		Map<String, Object> resultado = gson.fromJson(client.target(placeURL).request().get(String.class), HashMap.class);
		
		String responseStatus = (String) resultado.get("status");
		
		if(responseStatus != null && responseStatus.equals("OK")) {
			List<Map<String, Object>> lista = (List<Map<String, Object>>) resultado.get("predictions");
			
			if(lista.size() == 1) {
				Map<String, Object> placeFound = lista.get(0);
				
				String placeId = (String) placeFound.get("place_id");
				
				String placeDetailsURL = "https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyCmbN_CNrlEr_k91R85snsHip9GXS-l39s&placeid=" + URLEncoder.encode(placeId, "UTF-8");
				
				Map<String, Object> placeResult = gson.fromJson(client.target(placeDetailsURL).request().get(String.class), HashMap.class);
				
				responseStatus = (String) placeResult.get("status");
				
				if(responseStatus != null && responseStatus.equals("OK")) {
					Map<String, Object> enderecoEncontrado = (Map<String, Object>) placeResult.get("result");
					
					return buildAddress(enderecoEncontrado);
				}
			}
		}
		
		return new HashMap<>();
    }
	
	@SuppressWarnings("unchecked")
	private static Map<String, String> buildAddress(Map<String, Object> enderecoEncontrado) {
		String uf = null; //administrative_area_level_1 - short_name
		String municipio = null; //locality - long_name
		String numero = null; //street_number - long_name
		String cep = null; //postal_code - long_name
		String logradouro = null; //route - long_name
		String bairro = null; //administrative_area_level_4 = long_name
		
		List<Map<String, Object>> addressComponents = (List<Map<String, Object>>) enderecoEncontrado.get("address_components");
		
		for(Map<String, Object> addressComponent : addressComponents) {
			List<String> types = (List<String>) addressComponent.get("types");
			
			if(types.contains("administrative_area_level_1")) {
				uf = (String) addressComponent.get("short_name");
				uf = uf.toUpperCase();
			} else if((types.contains("locality") || types.contains("administrative_area_level_2")) && municipio == null) {
				municipio = (String) addressComponent.get("long_name");
				municipio = StringUtils.clearText(municipio.toUpperCase());
			} else if(types.contains("street_number")) {
				numero = (String) addressComponent.get("long_name");
				numero = numero.toUpperCase();
			} else if(types.contains("postal_code")) {
				cep = (String) addressComponent.get("long_name");
				cep = cep.toUpperCase();
			} else if(types.contains("route")) {
				logradouro = (String) addressComponent.get("long_name");
				logradouro = StringUtils.clearText(logradouro.toUpperCase());
			} else if(types.contains("administrative_area_level_4")) {
				bairro = (String) addressComponent.get("long_name");
				bairro = StringUtils.clearText(bairro.toUpperCase());
			}
		}
		
		Map<String, String> returnResult = new HashMap<>();
		
		returnResult.put("uf", uf);
		returnResult.put("municipio", municipio);
		returnResult.put("numero", numero);
		returnResult.put("cep", cep);
		returnResult.put("logradouro", logradouro);
		returnResult.put("bairro", bairro);
		
		return returnResult;
	}
}
