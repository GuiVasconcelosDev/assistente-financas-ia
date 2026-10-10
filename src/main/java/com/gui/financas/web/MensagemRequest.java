package com.gui.financas.web;

import jakarta.validation.constraints.NotBlank;

public record MensagemRequest(@NotBlank String mensagem) {
}
