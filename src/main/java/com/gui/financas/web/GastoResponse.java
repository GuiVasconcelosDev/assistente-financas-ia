package com.gui.financas.web;

import com.gui.financas.domain.CategoriaGasto;
import com.gui.financas.domain.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoResponse(Long id, String descricao, BigDecimal valor, CategoriaGasto categoria, LocalDate data) {

    public static GastoResponse from(Gasto g) {
        return new GastoResponse(g.getId(), g.getDescricao(), g.getValor(), g.getCategoria(), g.getData());
    }
}
