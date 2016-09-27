package com.dev.bruno.robot;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.dev.bruno.exception.AttemptsExceededException;

public class WebDocumentService {
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
//	private ProxyService proxyService;

	public Document getDocument(String url) throws Exception {
		return getDocument(url, null, null, false);
	}
	
	public Document getDocument(String url, Long connectionTimeout) throws Exception {
		return getDocument(url, connectionTimeout, null, false);
	}
	
	public Document getDocument(String url, Long connectionTimeout, Boolean usesProxy) throws Exception {
		return getDocument(url, connectionTimeout, null, usesProxy);
	}
	
	public Document getDocument(String url, Long connectionTimeout, String charset, Boolean usesProxy) throws Exception {
		if(charset == null) {
			charset = "UTF-8";
		}
		
		return Jsoup.parse(getDocumentAsStream(url, connectionTimeout, usesProxy, 1), charset, url);
	}
	
	public String getDocumentAsString(String url) throws Exception {
		return getDocumentAsString(url, null, false);
	}
	
	public String getDocumentAsString(String url, Long connectionTimeout) throws Exception {
		return getDocumentAsString(url, connectionTimeout, false);
	}
	
	public String getDocumentAsString(String url, Long connectionTimeout, Boolean usesProxy) throws Exception {
		return IOUtils.toString(getDocumentAsStream(url, connectionTimeout, usesProxy, 1));
	}
	
	private InputStream getDocumentAsStream(String url, Long connectionTimeout, Boolean usesProxy, Integer attempts) throws Exception {
		if(connectionTimeout == null) {
			connectionTimeout = 10000l;
		}
		
		if(attempts > 3) {
			throw new AttemptsExceededException();
		}
		
		URL website = new URL(url);
		
		HttpURLConnection httpUrlConnetion = null;
//		if(usesProxy) {
//			String proxyAddress = proxyService.getProxy();
//			String ip = proxyAddress.split(":")[0];
//			Integer port = Integer.parseInt(proxyAddress.split(":")[1]);
//			
//			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
//			httpUrlConnetion = (HttpURLConnection) website.openConnection(proxy);
//		} else {
			httpUrlConnetion = (HttpURLConnection) website.openConnection();
//		}
		
		try {
			httpUrlConnetion.setReadTimeout(connectionTimeout.intValue());
			httpUrlConnetion.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpUrlConnetion.addRequestProperty("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.6,en;q=0.4");
			httpUrlConnetion.addRequestProperty("Cache-Control", "no-cache");
			httpUrlConnetion.addRequestProperty("Pragma", "no-cache");
			httpUrlConnetion.addRequestProperty("Connection","close");
			httpUrlConnetion.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
			httpUrlConnetion.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
			httpUrlConnetion.connect();
			
			if(httpUrlConnetion.getHeaderFields().get("Content-Encoding") != null) {
				return new GZIPInputStream(httpUrlConnetion.getInputStream());
			} else {
				return httpUrlConnetion.getInputStream();
			}
//		}  catch(HttpStatusException e) {
//			if(!usesProxy || !Status.FORBIDDEN.equals(Status.fromStatusCode(e.getStatusCode()))) {
//				throw e;
//			}
//			
//			proxyService.blockProxy();
//			
//			logger.info("Tentativa[" + attempts + "] FALHOU: " + e.getMessage());
//			
//			return getDocumentAsStream(url, connectionTimeout, usesProxy, attempts + 1);
		} catch (Exception e) {
//			if(usesProxy) {
//				proxyService.blockProxy();
//			}
			
			logger.info("Tentativa[" + attempts + "] FALHOU: " + e.getMessage());
			
			return getDocumentAsStream(url, connectionTimeout, usesProxy, attempts + 1);
		}
	}
}