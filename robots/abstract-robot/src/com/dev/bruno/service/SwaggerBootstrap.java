package com.dev.bruno.service;

import io.swagger.jaxrs.config.BeanConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class SwaggerBootstrap extends HttpServlet {

	private static final long serialVersionUID = 4107514879366686247L;

	@Override
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		String basePath = config.getInitParameter("api.basepath");
		String host = null;
		String protocol = null;
        if (basePath != null) {
            String[] parts = basePath.split("://");
            if (parts.length > 1) {
            	protocol = parts[0];
                int pos = parts[1].indexOf("/");
                if (pos >= 0) {
                	host = parts[1].substring(0, pos);
                    basePath = parts[1].substring(pos);
                } else {
                    basePath = null;
                }
            }
        }

        String version = config.getInitParameter("api.version");
        
        String title = config.getInitParameter("api.title");
        
        String resourcePackage = config.getInitParameter("api.resourcepackage");

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(version);
        beanConfig.setBasePath(basePath);
        beanConfig.setSchemes(new String[] {protocol});
        beanConfig.setHost(host);
        beanConfig.setTitle(title);
        beanConfig.setResourcePackage(resourcePackage);
        beanConfig.setScan(true);
    }
}