package com.dev.bruno.dto;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.Expose;

public class ShowDTO implements Serializable {

	private static final long serialVersionUID = 2355398665476264819L;

	@Expose
	private String urlBase;
	
	@Expose
	private String localCaptado;
	
	@Expose
	private String nome;
	
	@Expose
	private Date dataRealizacao;
	
	@Expose
	private String uf;
	
	@Expose
	private String municipio;
	
	@Expose
	private String descricao;
	
	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}

	public String getLocalCaptado() {
		return localCaptado;
	}

	public void setLocalCaptado(String localCaptado) {
		this.localCaptado = localCaptado;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Date getDataRealizacao() {
		return dataRealizacao;
	}

	public void setDataRealizacao(Date dataRealizacao) {
		this.dataRealizacao = dataRealizacao;
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}