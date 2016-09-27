package com.dev.bruno.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.RobotStatus;
import com.dev.bruno.dto.RobotStatusDTO;

@Singleton
public class CrawlerService {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Resource(mappedName = "java:/JmsXA")
//	@Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;
 
	@Resource(name="crawlerQueue")
	private String crawlerQueue;
	
	private Connection connection;
    private Session session;
    
    private MessageProducer crawlerQueueProducer;
    
    private Destination crawlerQueueDestination;
    
    private void connect() throws JMSException, NamingException {
    	Context ctx = new InitialContext();
        connection = connectionFactory.createConnection();
        session = connection.createSession(true, Session.SESSION_TRANSACTED);
//            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        crawlerQueueDestination = (Destination) ctx.lookup(crawlerQueue);
        crawlerQueueProducer = session.createProducer(crawlerQueueDestination);
    }
    
    private void disconnect() throws JMSException {
        if (connection != null) {
        	connection.close();
        }
    }
    
    public RobotStatusDTO run(CrawlerDTO dto) {
    	RobotStatusDTO response = new RobotStatusDTO();
    	response.setStatus(RobotStatus.CRAWLING);
    	
    	List<String> urls = dto.getUrlsToCrawling();
    	
    	Integer elementsPerPage = urls.size() / dto.getThreads().intValue();
    	
    	if(urls.size() % dto.getThreads() != 0) {
    		elementsPerPage++;
    	}
    	
    	Integer start = 0;
    	
    	while(start < urls.size()) {
    		Integer end = Math.min(elementsPerPage + start, urls.size());
    		
    		List<String> newUrls = new ArrayList<>(urls.subList(start, end));
    		
    		CrawlerDTO newDTO = new CrawlerDTO();
    		newDTO.setConnectionTimeout(dto.getConnectionTimeout());
    		newDTO.setDelay(dto.getDelay());
    		newDTO.setDocumentURLRegex(dto.getDocumentURLRegex());
    		newDTO.setEndDepth(dto.getEndDepth());
    		newDTO.setThreads(dto.getThreads());
    		newDTO.setCallbackUrl(dto.getCallbackUrl());
    		newDTO.setCallbackHeaders(dto.getCallbackHeaders());
    		newDTO.setSourceURLRegex(dto.getSourceURLRegex());
    		newDTO.setDocumentType(dto.getDocumentType());
    		newDTO.setUrlsToCrawling(newUrls);
    		
    		start += elementsPerPage;
    		
    		try {
    			add(newDTO);
    		} catch(Exception e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
    			response.setStatus(RobotStatus.FAILED);
    			response.setMensage(e.getMessage());
    		}
    	}
    	
		return response;
	}
    
    private void add(CrawlerDTO dto) throws JMSException, NamingException {
    	connect();
    	
       	ObjectMessage message = session.createObjectMessage(dto);
        crawlerQueueProducer.send(message);
        
        disconnect();
    }
}