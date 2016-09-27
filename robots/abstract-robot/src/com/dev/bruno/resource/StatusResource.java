package com.dev.bruno.resource;

import io.swagger.annotations.Api;

import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.dto.RobotStatusDTO;

@Stateless
@Path("/robot")
@Produces(MediaType.APPLICATION_JSON)
@Api
public class StatusResource {

	@GET
	@Path("/status")
	public RobotStatusDTO getStatus() throws JMSException {
		return new RobotStatusDTO();
	}
}