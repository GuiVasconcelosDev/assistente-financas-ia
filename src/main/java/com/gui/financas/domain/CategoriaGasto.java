package com.gui.financas.domain;

import java.text.Normalizer;
import java.util.Locale;

public enum CategoriaGasto {
    ALIMENTACAO, TRANSPORTE, MORADIA, LAZER, SAUDE, EDUCACAO, OUTROS;

    /**
     * Converte um texto livre (ex.: "Alimentação") na categoria correspondente.
     * Ignora acentos e maiúsculas. Texto vazio ou desconhecido vira OUTROS.
     */
    public static CategoriaGasto deTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return OUTROS;
        }
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        for (CategoriaGasto categoria : values()) {
            if (categoria.name().equals(normalizado)) {
                return categoria;
            }
        }
        return OUTROS;
    }
}
