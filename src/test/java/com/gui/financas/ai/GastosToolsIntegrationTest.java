package com.gui.financas.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa as ferramentas da IA com o banco H2 real, SEM chamar a OpenAI.
 * É exatamente o que o modelo executa quando decide usar uma ferramenta.
 */
@SpringBootTest
class GastosToolsIntegrationTest {

    @Autowired
    private GastosTools tools;

    @Test
    void registraEConsultaTotalPorPeriodoECategoria() {
        String resposta = tools.registrarGasto("Almoço", 30.0, "alimentação", "2020-03-10");
        tools.registrarGasto("Uber", 20.5, "TRANSPORTE", "2020-03-12");

        assertThat(resposta).contains("Gasto registrado").contains("R$ 30.00");
        assertThat(tools.totalGasto("2020-03-01", "2020-03-31", null)).contains("R$ 50.50");
        assertThat(tools.totalGasto("2020-03-01", "2020-03-31", "ALIMENTACAO")).contains("R$ 30.00");
    }

    @Test
    void resumoPorCategoriaListaDaMaiorParaAMenor() {
        tools.registrarGasto("Aluguel", 900.0, "MORADIA", "2021-05-05");
        tools.registrarGasto("Cinema", 40.0, "LAZER", "2021-05-08");

        String resumo = tools.resumoPorCategoria("2021-05-01", "2021-05-31");

        assertThat(resumo).contains("MORADIA: R$ 900.00").contains("LAZER: R$ 40.00");
        assertThat(resumo.indexOf("MORADIA")).isLessThan(resumo.indexOf("LAZER"));
    }

    @Test
    void devolveErroLegivelQuandoOValorEInvalido() {
        assertThat(tools.registrarGasto("Teste", -5.0, "OUTROS", null)).startsWith("Erro:").contains("maior que zero");
    }

    @Test
    void devolveErroLegivelQuandoADataEstaNoFuturo() {
        assertThat(tools.registrarGasto("Teste", 5.0, "OUTROS", "2999-01-01")).contains("futuro");
    }

    @Test
    void devolveErroLegivelQuandoOFormatoDaDataEInvalido() {
        assertThat(tools.totalGasto("ontem", "hoje", null)).contains("AAAA-MM-DD");
    }
}
