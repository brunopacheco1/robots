package com.dev.bruno.resource;

import io.swagger.annotations.Api;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.RobotStatusDTO;
import com.dev.bruno.service.CrawlerService;

@Stateless
@Path("/robot")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api
public class CrawlerResource {

	@EJB
	private CrawlerService crawlerService;
	
	@POST
	@Path("/crawler/run")
	public RobotStatusDTO runCrawler(CrawlerDTO dto) {
		return crawlerService.run(dto);
	}
}