package com.dev.bruno.resource;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.dto.CrawlerDTO;
import com.dev.bruno.dto.RobotStatusDTO;
import com.dev.bruno.service.CrawlerService;

import io.swagger.annotations.Api;

@Stateless
@Path("/robot")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api
public class CrawlerResource {

	@Inject
	private CrawlerService crawlerService;
	
	@POST
	@Path("/crawler/run")
	public RobotStatusDTO runCrawler(CrawlerDTO dto) {
		return crawlerService.run(dto);
	}
}