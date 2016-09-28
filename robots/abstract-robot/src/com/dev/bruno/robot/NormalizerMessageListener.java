package com.dev.bruno.robot;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.net.ssl.SSLProtocolException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jsoup.HttpStatusException;
import org.reflections.Reflections;

import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.exception.AttemptsExceededException;
import com.dev.bruno.exception.NoProxyException;
import com.dev.bruno.service.NormalizerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NormalizerMessageListener implements MessageListener {

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Inject
	protected NormalizerService normalizerService;
	
	private Map<String, Object> capturedDocuments;
	
	private Set<String> visitedLinks;
	
	private Set<String> problemLinks;
	
	protected Map<String, String> ufs;
	
	private Map<DocumentType, Class<? extends Normalizer<?>>> normalizers = new HashMap<>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostConstruct
	private void init() {
		Reflections reflections = new Reflections("com.dev.bruno.robot");

		Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Normalizing.class);
		
		for(Class clazz : annotatedClasses) {
			Normalizing normalizing = (Normalizing) clazz.getAnnotation(Normalizing.class);
			
			if(normalizing != null && normalizing.documentType() != null) {
				normalizers.put(normalizing.documentType(), clazz);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void onMessage(Message message) {
		capturedDocuments = new HashMap<>();
		visitedLinks = new HashSet<>();
		problemLinks = new HashSet<>();
		
		ObjectMessage objMsg = (ObjectMessage) message;
		
		NormalizerDTO dto = null; 
        try {
        	dto = (NormalizerDTO) objMsg.getObject();
        } catch (JMSException e) {
        	dto = null;
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        
        if(dto == null || dto.getDocumentType() == null) {
			logger.log(Level.SEVERE, "NORMALIZADOR SEM CONFIGURAÇÃO.");
			return;
		}
        
        Class<? extends Normalizer<?>> normalizerClass = normalizers.get(dto.getDocumentType());
		
		if (normalizerClass == null) {
			logger.log(Level.SEVERE, "CLASSE DE NORMALIZAÇÃO DE " + dto.getDocumentType() + " NÃO ENCONTRADO.");
			return;
		}
		
		Normalizer normalizer = null;
		try {
			normalizer = (Normalizer) normalizerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			normalizer = null;
			logger.log(Level.SEVERE, "ERRO NA BUSCA DA CLASSE DO NORMALIZADOR: " + e.getMessage());
		}
		
		if(normalizer == null) {
			return;
		}
        
        normalizing(normalizer, dto);
		
        callback(normalizer, dto);
	}
	
	protected void addProblemLink(String link) {
		problemLinks.add(link);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void normalizing(Normalizer normalizer, NormalizerDTO dto) {
		for(String url : dto.getUrlsToNormalizing()) {
			if(visitedLinks.contains(url)) {
				continue;
			}
			
			visitedLinks.add(url);
			
	        Long time = System.currentTimeMillis();
	        
	        try {
	        	normalizer.run(dto, url);
	        	
	        	if(normalizer.getProblemLink()) {
	        		addProblemLink(url);
	        	} else {
	        		capturedDocuments.putAll(normalizer.getCapturedDocuments());
	        	}
	        } catch(AttemptsExceededException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[ATTEMPTS_EXCEEDED] AO LINK: " + url);
	        } catch(NoProxyException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[NO_PROXY] AO LINK: " + url);
	        } catch(SSLProtocolException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[SSL_REFUSED] AO LINK: " + url);
	        	addProblemLink(url);
	        } catch(SocketTimeoutException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[TIME_OUT] AO LINK: " + url);
	        	addProblemLink(url);
	        } catch(HttpStatusException e) {
	    		logger.log(Level.SEVERE, "ERRO NO ACESSO[" + Status.fromStatusCode(e.getStatusCode()) + "] AO LINK: " + url);
	    		addProblemLink(url);
	        } catch(IOException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[IO_EXCEPTION] AO LINK: " + url);
	        	addProblemLink(url);
	        } catch (Exception e) {
	        	logger.log(Level.SEVERE, "ERRO NA CAPTACAO DO LINK: " + url);
	            logger.log(Level.SEVERE, e.getMessage(), e);
	            addProblemLink(url);
	        }
	        
	    	time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Documento[%s] normalizado em %sms.", url, time));
    	
			if(dto.getDelay() != null) {
		    	try {
					Thread.sleep(dto.getDelay());
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
        }
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void callback(Normalizer normalizer, NormalizerDTO dto) {
		if (capturedDocuments.isEmpty() && problemLinks.isEmpty()) {
			logger.info("Nenhum documento ou problemas foram encontrados.");
			return;
		}
		
		if(dto.getCallbackUrl() == null) {
			logger.info("Callback não foi configurado.");
			return;
		}
		
		Long time = System.currentTimeMillis();

		Integer status = 200;

		Gson gson = new GsonBuilder().setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss").create();

		Client client = ClientBuilder.newClient();

		try {
			Builder builder = client.target(dto.getCallbackUrl()).request().accept(MediaType.APPLICATION_JSON);
			
			for(String header : dto.getCallbackHeaders().keySet()) {
				String value = dto.getCallbackHeaders().get(header);
				
				builder.header(header, value);
			}
			
			builder.header("Robot", "yes");
			
			Response response = builder.post(Entity.entity(gson.toJson(normalizer.genarateNormalizingResult(capturedDocuments, problemLinks)), MediaType.APPLICATION_JSON_TYPE));
			status = response.getStatus();
		} catch (Exception e) {
			status = 500;
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		time = System.currentTimeMillis() - time;

		logger.info(String.format("Documentos enviados: status[%s] >> %sms", status, time));
	}
}