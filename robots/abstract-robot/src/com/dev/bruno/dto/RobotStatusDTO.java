package com.dev.bruno.dto;

import java.io.Serializable;

public class RobotStatusDTO implements Serializable {

	private static final long serialVersionUID = -1146712076677681794L;

	private RobotStatus status = RobotStatus.ACTIVE;
	
	private String mensage;

	public RobotStatus getStatus() {
		return status;
	}

	public void setStatus(RobotStatus status) {
		this.status = status;
	}

	public String getMensage() {
		return mensage;
	}

	public void setMensage(String mensage) {
		this.mensage = mensage;
	}
}