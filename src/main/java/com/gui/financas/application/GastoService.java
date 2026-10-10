package com.gui.financas.application;

import com.gui.financas.domain.CategoriaGasto;
import com.gui.financas.domain.Gasto;
import com.gui.financas.domain.GastoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Casos de uso de gastos. É usado tanto pelos endpoints REST quanto pelas
 * ferramentas (Tool Calling) da IA, então as validações valem para os dois caminhos.
 */
@Service
public class GastoService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("1000000.00");
    private static final int DESCRICAO_MAXIMA = 120;
    private static final int QUANTIDADE_MAXIMA = 20;

    private final GastoRepository repository;
    private final Clock clock;

    public GastoService(GastoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Gasto registrar(String descricao, BigDecimal valor, String categoria, LocalDate data) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do gasto é obrigatória.");
        }
        if (descricao.trim().length() > DESCRICAO_MAXIMA) {
            throw new IllegalArgumentException("A descrição deve ter no máximo " + DESCRICAO_MAXIMA + " caracteres.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor do gasto deve ser maior que zero.");
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new IllegalArgumentException("O valor do gasto excede o limite de R$ 1.000.000,00.");
        }

        LocalDate hoje = LocalDate.now(clock);
        LocalDate dataGasto = data != null ? data : hoje;
        if (dataGasto.isAfter(hoje)) {
            throw new IllegalArgumentException("A data do gasto não pode estar no futuro.");
        }

        return repository.save(new Gasto(
                descricao.trim(),
                valor.setScale(2, RoundingMode.HALF_UP),
                CategoriaGasto.deTexto(categoria),
                dataGasto));
    }

    /** Total gasto no período; se categoria for informada, filtra por ela. */
    @Transactional(readOnly = true)
    public BigDecimal total(LocalDate inicio, LocalDate fim, String categoria) {
        validarPeriodo(inicio, fim);
        BigDecimal total = (categoria == null || categoria.isBlank())
                ? repository.somarPorPeriodo(inicio, fim)
                : repository.somarPorCategoriaEPeriodo(CategoriaGasto.deTexto(categoria), inicio, fim);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Transactional(readOnly = true)
    public Map<CategoriaGasto, BigDecimal> resumoPorCategoria(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        Map<CategoriaGasto, BigDecimal> resumo = new LinkedHashMap<>();
        for (Object[] linha : repository.somarAgrupadoPorCategoria(inicio, fim)) {
            resumo.put((CategoriaGasto) linha[0], (BigDecimal) linha[1]);
        }
        return resumo;
    }

    @Transactional(readOnly = true)
    public List<Gasto> ultimos(int quantidade) {
        if (quantidade < 1 || quantidade > QUANTIDADE_MAXIMA) {
            throw new IllegalArgumentException("A quantidade deve estar entre 1 e " + QUANTIDADE_MAXIMA + ".");
        }
        return repository.findAllByOrderByDataDescIdDesc(PageRequest.of(0, quantidade));
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Informe a data inicial e a data final do período.");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da data final.");
        }
    }
}
