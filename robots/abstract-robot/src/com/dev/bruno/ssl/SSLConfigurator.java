package com.dev.bruno.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

@Singleton
@Startup
public class SSLConfigurator {

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	@PostConstruct
	private void sslConfiguration() {
		SSLContext sc = null;
		
		try {
			sc = SSLContext.getInstance("TLSv1.2");
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		try {
			sc.init(null, new TrustManager[] { new IgnoreSSLTrustManager() }, new java.security.SecureRandom());
		} catch (KeyManagementException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		HttpsURLConnection.setDefaultSSLSocketFactory(new IgnoreSSLSocketFactory(sc.getSocketFactory()));
		HttpsURLConnection.setDefaultHostnameVerifier(new IgnoreSSLHostnameVerifier());
	}
}