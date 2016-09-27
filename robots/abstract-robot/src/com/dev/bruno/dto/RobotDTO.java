package com.dev.bruno.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class RobotDTO implements Serializable {

	private static final long serialVersionUID = -5895880963733776596L;
	
	private String callbackUrl;
	
	private Map<String, String> callbackHeaders = new HashMap<>();

	private Long connectionTimeout = 10000l;
	
	private Long delay = 0l;
	
	private Long threads = 1l;

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public Map<String, String> getCallbackHeaders() {
		return callbackHeaders;
	}

	public void setCallbackHeaders(Map<String, String> callbackHeaders) {
		this.callbackHeaders = callbackHeaders;
	}

	public Long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Long getThreads() {
		return threads;
	}

	public void setThreads(Long threads) {
		this.threads = threads;
	}
}