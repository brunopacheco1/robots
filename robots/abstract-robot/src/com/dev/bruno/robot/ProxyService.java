package com.dev.bruno.robot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dev.bruno.exception.NoProxyException;

@Singleton
public class ProxyService {

	private List<String> newIps = new ArrayList<>();
	
	private String currentIp;
	
	private List<String> blockedIps = new ArrayList<>();
	
	@PostConstruct
	public void updateList() {
		newIps = new ArrayList<>();
		currentIp = null;

		try {
			Document document = Jsoup.connect("http://www.proxynova.com/proxy-server-list/country-br/").get();
			
			for(Element proxy : document.select("table#tbl_proxy_list > tbody > tr")) {
				if(proxy.select("td").size() != 7) {
					continue;
				}
				
				String ip = proxy.select("td").get(0).text();
				String port = proxy.select("td").get(1).text();
				
				Double speed = Double.parseDouble(proxy.select("td div.progress-bar").first().attr("data-value"));
				
				Double uptime = Double.parseDouble(proxy.select("td").get(4).text().replaceAll("\\D", ""));
				
				if(speed >= 50 && uptime >= 50) {
					newIps.add(ip + ":" + port);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getProxy() throws NoProxyException {
		if(newIps.isEmpty()) {
			throw new NoProxyException();
		}
		
		if(currentIp == null) {
			currentIp = newIps.remove(0);
		}
		
		return currentIp;
	}
	
	public void blockProxy() {
		if(currentIp != null) {
			blockedIps.add(currentIp);
			
			currentIp = null;
		}
	}
}