package com.dev.bruno.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

public class IgnoreSSLSocketFactory extends SSLSocketFactory {

	private final SSLSocketFactory sslSocketFactory;

	  public IgnoreSSLSocketFactory(SSLSocketFactory socketFactory) {
	    this.sslSocketFactory = socketFactory;
	  }

	  @Override
	  public String[] getDefaultCipherSuites() {
	    return sslSocketFactory.getDefaultCipherSuites();
	  }

	  @Override
	  public String[] getSupportedCipherSuites() {
	    return sslSocketFactory.getSupportedCipherSuites();
	  }

	  @Override
	  public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
	    return sslSocketFactory.createSocket(socket, "", port, autoClose);
	  }

	  @Override
	  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
	    return createSocket(new Socket(host, port), host, port, true);
	  }

	  @Override
	  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
	    return createSocket(new Socket(host, port, localHost, localPort), host, port, true);
	  }

	  @Override
	  public Socket createSocket(InetAddress host, int port) throws IOException {
	    return sslSocketFactory.createSocket(host, port);
	  }

	  @Override
	  public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort) throws IOException {
	    return createSocket(new Socket(host, port, localHost, localPort), host.getHostName(), port, true);
	  }
}