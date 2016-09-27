package com.dev.bruno.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class IgnoreSSLHostnameVerifier implements HostnameVerifier {

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}