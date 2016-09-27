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

import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.RobotStatus;
import com.dev.bruno.dto.RobotStatusDTO;

@Singleton
public class NormalizerService {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Resource(mappedName = "java:/JmsXA")
//	@Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;
 
	@Resource(name="normalizerQueue")
	private String normalizerQueue;
	
	private Connection connection;
    private Session session;
    
    private MessageProducer normalizerQueueProducer;
    private Destination normalizerQueueDestination;
    
    private void connect() throws NamingException, JMSException {
    	Context ctx = new InitialContext();
        connection = connectionFactory.createConnection();
        session = connection.createSession(true, Session.SESSION_TRANSACTED);
//          session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        normalizerQueueDestination = (Destination) ctx.lookup(normalizerQueue);
        normalizerQueueProducer = session.createProducer(normalizerQueueDestination);
    }
    
    private void disconnect() throws JMSException {
        if (connection != null) {
        	connection.close();
        }
    }
    
    public RobotStatusDTO run(NormalizerDTO dto) {
    	RobotStatusDTO response = new RobotStatusDTO();
    	response.setStatus(RobotStatus.NORMALIZING);
    	
    	List<String> urls = dto.getUrlsToNormalizing();
    	
    	Integer elementsPerPage = urls.size() / dto.getThreads().intValue();
    	
    	if(urls.size() % dto.getThreads() != 0) {
    		elementsPerPage++;
    	}
    	
    	Integer start = 0;
    	
    	while(start < urls.size()) {
    		Integer end = Math.min(elementsPerPage + start, urls.size());
    		
    		List<String> newUrls = new ArrayList<>(urls.subList(start, end));
    		
    		NormalizerDTO newDTO = new NormalizerDTO();
			newDTO.setUrlsToNormalizing(newUrls);
			newDTO.setDelay(dto.getDelay());
			newDTO.setConnectionTimeout(dto.getConnectionTimeout());
			newDTO.setThreads(dto.getThreads());
			newDTO.setCallbackHeaders(dto.getCallbackHeaders());
			newDTO.setCallbackUrl(dto.getCallbackUrl());
			newDTO.setDocumentType(dto.getDocumentType());
    		
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
    
    private void add(NormalizerDTO dto) throws NamingException, JMSException {
    	connect();
    	
    	ObjectMessage message = session.createObjectMessage(dto);
        normalizerQueueProducer.send(message);
        
        disconnect();
    }
}