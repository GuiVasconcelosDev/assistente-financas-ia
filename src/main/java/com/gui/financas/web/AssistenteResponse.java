package com.gui.financas.web;

import com.fasterxml.jackson.annotation.JsonInclude;

/** transcricao só aparece quando a entrada foi um áudio. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssistenteResponse(String transcricao, String resposta) {
}
