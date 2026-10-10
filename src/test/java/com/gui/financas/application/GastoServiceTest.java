package com.gui.financas.application;

import com.gui.financas.domain.CategoriaGasto;
import com.gui.financas.domain.Gasto;
import com.gui.financas.domain.GastoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GastoServiceTest {

    // Relógio fixo: "hoje" é sempre 2026-10-10 nos testes.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private final GastoRepository repository = mock(GastoRepository.class);
    private final GastoService service = new GastoService(repository, clock);

    @BeforeEach
    void configurarMock() {
        when(repository.save(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void registraComDataDeHojeQuandoNaoInformada() {
        Gasto gasto = service.registrar("  Almoço ", new BigDecimal("32.5"), "alimentação", null);

        assertThat(gasto.getDescricao()).isEqualTo("Almoço");
        assertThat(gasto.getValor()).isEqualByComparingTo("32.50");
        assertThat(gasto.getCategoria()).isEqualTo(CategoriaGasto.ALIMENTACAO);
        assertThat(gasto.getData()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    @Test
    void rejeitaValorZeroOuNegativo() {
        assertThatThrownBy(() -> service.registrar("Café", BigDecimal.ZERO, "OUTROS", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
        assertThatThrownBy(() -> service.registrar("Café", new BigDecimal("-3"), "OUTROS", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaDescricaoVazia() {
        assertThatThrownBy(() -> service.registrar("   ", new BigDecimal("10"), "OUTROS", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descrição");
    }

    @Test
    void rejeitaDataNoFuturoENaoSalva() {
        assertThatThrownBy(() -> service.registrar("Cinema", new BigDecimal("40"), "LAZER", LocalDate.of(2026, 10, 11)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futuro");
        verify(repository, never()).save(any(Gasto.class));
    }

    @Test
    void totalSemGastosRetornaZero() {
        when(repository.somarPorPeriodo(any(LocalDate.class), any(LocalDate.class))).thenReturn(null);

        BigDecimal total = service.total(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null);

        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    void rejeitaPeriodoInvertido() {
        assertThatThrownBy(() -> service.total(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inicial");
    }

    @Test
    void rejeitaQuantidadeForaDoLimite() {
        assertThatThrownBy(() -> service.ultimos(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.ultimos(21)).isInstanceOf(IllegalArgumentException.class);
    }
}
