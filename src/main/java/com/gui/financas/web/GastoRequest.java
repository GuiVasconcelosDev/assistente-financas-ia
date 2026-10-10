package com.gui.financas.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoRequest(
        @NotBlank String descricao,
        @NotNull @Positive BigDecimal valor,
        String categoria,
        LocalDate data
) {
}
