package com.dev.bruno.resource;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.RobotStatusDTO;
import com.dev.bruno.service.NormalizerService;

import io.swagger.annotations.Api;

@Stateless
@Path("/robot")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api
public class NormalizerResource {

	@Inject
	private NormalizerService normalizerService;
	
	@POST
	@Path("/normalizer/run")
	public RobotStatusDTO runNormalizer(NormalizerDTO dto) {
		return normalizerService.run(dto);
	}
}