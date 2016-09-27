package com.dev.bruno.robot;

import com.dev.bruno.dto.ShowDTO;
import com.dev.bruno.utils.HashUtils;

public abstract class ShowNormalizer extends Normalizer<ShowDTO> {

	protected String gerarChave(ShowDTO show) {
		String chave = show.getLocalCaptado() + "_" + show.getDataRealizacao().getTime();
		
		return HashUtils.getHash(chave.toLowerCase());
	}
}
