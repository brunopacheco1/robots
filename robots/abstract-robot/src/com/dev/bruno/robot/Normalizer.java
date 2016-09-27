package com.dev.bruno.robot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.dev.bruno.dto.NormalizerDTO;
import com.dev.bruno.dto.NormalizingResultDTO;

public abstract class Normalizer<T> {

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	protected WebDocumentService documentService = new WebDocumentService();
	
	private Map<String, T> capturedDocuments = new HashMap<>();
	
	private Boolean problemLink = false;
	
	protected Map<String, String> ufs;
	
	public Normalizer() {
		ufs = new HashMap<>();
		
		ufs.put("acre", "AC");
		ufs.put("alagoas", "AL");
		ufs.put("amazonas", "AM");
		ufs.put("amapa", "AP");
		ufs.put("bahia", "BA");
		ufs.put("ceara", "CE");
		ufs.put("distrito federal", "DF");
		ufs.put("espirito santo", "ES");
		ufs.put("goias", "GO");
		ufs.put("maranhao", "MA");
		ufs.put("minas gerais", "MG");
		ufs.put("mato grosso do sul", "MS");
		ufs.put("mato grosso", "MT");
		ufs.put("para", "PA");
		ufs.put("paraiba", "PB");
		ufs.put("pernambuco", "PE");
		ufs.put("piaui", "PI");
		ufs.put("parana", "PR");
		ufs.put("rio de janeiro", "RJ");
		ufs.put("rio grande do norte", "RN");
		ufs.put("rondonia", "RO");
		ufs.put("roraima", "RR");
		ufs.put("rio grande do sul", "RS");
		ufs.put("santa catarina", "SC");
		ufs.put("sergipe", "SE");
		ufs.put("sao paulo", "SP");
		ufs.put("tocantins", "TO");
		
		ufs.put("AC", "AC");
		ufs.put("AL", "AL");
		ufs.put("AM", "AM");
		ufs.put("AP", "AP");
		ufs.put("BA", "BA");
		ufs.put("CE", "CE");
		ufs.put("DF", "DF");
		ufs.put("ES", "ES");
		ufs.put("GO", "GO");
		ufs.put("MA", "MA");
		ufs.put("MG", "MG");
		ufs.put("MS", "MS");
		ufs.put("MT", "MT");
		ufs.put("PA", "PA");
		ufs.put("PB", "PB");
		ufs.put("PE", "PE");
		ufs.put("PI", "PI");
		ufs.put("PR", "PR");
		ufs.put("RJ", "RJ");
		ufs.put("RN", "RN");
		ufs.put("RO", "RO");
		ufs.put("RR", "RR");
		ufs.put("RS", "RS");
		ufs.put("SC", "SC");
		ufs.put("SE", "SE");
		ufs.put("SP", "SP");
		ufs.put("TO", "TO");
	}
	
	public Boolean getProblemLink() {
		return problemLink;
	}

	protected void setProblemLink(Boolean problemLink) {
		this.problemLink = problemLink;
	}

	public abstract void run(NormalizerDTO normalizerDTO, String url) throws Exception;
	
	protected void addCapturedDocument(T document) {
		capturedDocuments.put(gerarChave(document), document);
	}
	
	public Map<String, T> getCapturedDocuments() {
		return capturedDocuments;
	}
	
	@SuppressWarnings("unchecked")
	public NormalizingResultDTO<T> genarateNormalizingResult(Map<String, Object> capturedDocuments, Set<String> problemLinks) {
		NormalizingResultDTO<T> responseDto = new NormalizingResultDTO<>();
		List<T> documents = new ArrayList<>((Collection<T>) capturedDocuments.values());
		responseDto.setCapturedDocuments(documents);
		responseDto.setProblemLinks(new ArrayList<>(problemLinks));
		
		return responseDto;
	}

	protected abstract String gerarChave(T document);
}