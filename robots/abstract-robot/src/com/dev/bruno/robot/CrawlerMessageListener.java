package com.dev.bruno.robot;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
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

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.CrawlingResultDTO;
import com.dev.bruno.dto.DocumentType;
import com.dev.bruno.exception.AttemptsExceededException;
import com.dev.bruno.exception.NoProxyException;
import com.dev.bruno.service.CrawlerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CrawlerMessageListener implements MessageListener {
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Inject
	protected CrawlerService crawlerService;
	
	private Set<String> capturedLinks;
	
	private Set<String> visitedLinks;
	
	private Map<DocumentType, Class<? extends Crawler>> crawlers = new HashMap<>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostConstruct
	private void init() {
		Reflections reflections = new Reflections("com.dev.bruno.robot");

		Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Crawling.class);
		
		for(Class clazz : annotatedClasses) {
			Crawling crawling = (Crawling) clazz.getAnnotation(Crawling.class);
			
			if(crawling != null && crawling.documentType() != null) {
				crawlers.put(crawling.documentType(), clazz);
			}
		}
	}
	
	@Override
    public void onMessage(Message message) {
		capturedLinks = new HashSet<>();
		visitedLinks = new HashSet<>();
		
		ObjectMessage objMsg = (ObjectMessage) message;
		
		CrawlerDTO dto = null;
        try {
        	dto = (CrawlerDTO) objMsg.getObject();
        } catch (JMSException e) {
        	dto = null;
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        
        if(dto == null) {
        	return;
        }
        
		crawling(dto);
		
		
    	callback(dto);
    }
	
	protected void crawling(CrawlerDTO dto) {
		if(dto == null || dto.getDocumentType() == null) {
			logger.log(Level.SEVERE, "CRAWLER SEM CONFIGURAÇÃO.");
			return;
		}
		
		Class<? extends Crawler> crawlerClass = crawlers.get(dto.getDocumentType());
		
		if (crawlerClass == null) {
			logger.log(Level.SEVERE, "CLASSE DE CRAWLING DE " + dto.getDocumentType() + " NÃO ENCONTRADO.");
			return;
		}

		Crawler crawler = null;
		try {
			crawler = crawlerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			crawler = null;
			logger.log(Level.SEVERE, "ERRO NA BUSCA DA CLASSE DO CRAWLER: " + e.getMessage());
		}
		
		if(crawler == null) {
			return;
		}
		
		for(String url : dto.getUrlsToCrawling()) {
			if(visitedLinks.contains(url)) {
				continue;
			}
			
			visitedLinks.add(url);
			
        	Long time = System.currentTimeMillis();
        	
	        try {
	        	crawler.run(dto, url);
	        	capturedLinks.addAll(crawler.getCapturedLinks());
	        	
	        	if(!crawler.getUrlsToCrawling().isEmpty() && dto.getDepth() < dto.getEndDepth()) {
	        		CrawlerDTO newCrawlerDTO = new CrawlerDTO();
					
					newCrawlerDTO.setDepth(dto.getDepth() + 1);
					newCrawlerDTO.setEndDepth(dto.getEndDepth());
					newCrawlerDTO.setConnectionTimeout(dto.getConnectionTimeout());
					newCrawlerDTO.setSourceURLRegex(dto.getSourceURLRegex());
					newCrawlerDTO.setDocumentURLRegex(dto.getDocumentURLRegex());
					newCrawlerDTO.setDocumentType(dto.getDocumentType());
					newCrawlerDTO.setUrlsToCrawling(new ArrayList<>(crawler.getUrlsToCrawling()));
					
					crawling(newCrawlerDTO);
	        	}
	        } catch(AttemptsExceededException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[ATTEMPTS_EXCEEDED] AO LINK: " + url);
	        } catch(NoProxyException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[NO_PROXY] AO LINK: " + url);
	        } catch(SSLProtocolException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[SSL_REFUSED] AO LINK: " + url);
	        } catch(ConnectException e){
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[CONNETION_REFUSED] AO LINK: " + url);
	        } catch(SocketTimeoutException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[TIME_OUT] AO LINK: " + url);
	        } catch(HttpStatusException e) {
	    		logger.log(Level.SEVERE, "ERRO NO ACESSO[" + Status.fromStatusCode(e.getStatusCode()) + "] AO LINK: " + url);
	        } catch(IOException e) {
	        	logger.log(Level.SEVERE, "ERRO NO ACESSO[IO_EXCEPTION] AO LINK: " + url);
	        } catch (Exception e) {
	        	logger.log(Level.SEVERE, "ERRO NA CAPTACAO DO LINK: " + url);
	            logger.log(Level.SEVERE, e.getMessage(), e);
	        }
	        
	        time = System.currentTimeMillis() - time;
			
			logger.info(String.format("Fonte[%s - %s/%s] visitada em %sms.", url, dto.getDepth(), dto.getEndDepth(), time));
	    	
			if(dto.getDelay() != null) {
		    	try {
					Thread.sleep(dto.getDelay());
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
        }
	}
	
	private void callback(CrawlerDTO dto) {
		if(capturedLinks.isEmpty()) {
			logger.info("Nenhum link foi capturado.");
			return;
		}
		
		if(dto.getCallbackUrl() == null) {
			logger.info("Callback não foi configurado.");
			return;
		}
		
		Long time = System.currentTimeMillis();
		
		Integer status = 200;

		Gson gson = new GsonBuilder().serializeNulls().create();
		Client client = ClientBuilder.newClient();
		
		try {
			Builder builder = client.target(dto.getCallbackUrl()).request().accept(MediaType.APPLICATION_JSON);
			
			for(String header : dto.getCallbackHeaders().keySet()) {
				String value = dto.getCallbackHeaders().get(header);
				
				builder.header(header, value);
			}
			
			builder.header("Robot", "yes");
			
			CrawlingResultDTO resultDTO = new CrawlingResultDTO();
			resultDTO.setCapturedLinks(new ArrayList<>(capturedLinks));
			
			Response response = builder.post(Entity.entity(gson.toJson(resultDTO), MediaType.APPLICATION_JSON_TYPE));
			status = response.getStatus();
		} catch (Exception e) {
			status = 500;
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		time = System.currentTimeMillis() - time;

		logger.info(String.format("Links enviados: status[%s] >> %sms", status, time));
	}
}