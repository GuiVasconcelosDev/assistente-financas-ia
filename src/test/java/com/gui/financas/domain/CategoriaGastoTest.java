package com.gui.financas.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriaGastoTest {

    @Test
    void ignoraAcentoEMaiusculas() {
        assertThat(CategoriaGasto.deTexto("Alimentação")).isEqualTo(CategoriaGasto.ALIMENTACAO);
        assertThat(CategoriaGasto.deTexto("  saude ")).isEqualTo(CategoriaGasto.SAUDE);
    }

    @Test
    void textoDesconhecidoOuVazioViraOutros() {
        assertThat(CategoriaGasto.deTexto("pets")).isEqualTo(CategoriaGasto.OUTROS);
        assertThat(CategoriaGasto.deTexto("")).isEqualTo(CategoriaGasto.OUTROS);
        assertThat(CategoriaGasto.deTexto(null)).isEqualTo(CategoriaGasto.OUTROS);
    }
}
