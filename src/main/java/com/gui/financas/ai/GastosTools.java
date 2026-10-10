package com.gui.financas.ai;

import com.gui.financas.application.GastoService;
import com.gui.financas.domain.CategoriaGasto;
import com.gui.financas.domain.Gasto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ferramentas (Tool Calling) que o modelo pode chamar.
 * Cada método delega para o GastoService, então a IA nunca acessa o banco diretamente.
 * Quando uma regra de negócio falha, devolvemos a mensagem de erro como texto
 * para o modelo explicar ao usuário em vez de quebrar a conversa.
 */
@Component
public class GastosTools {

    private static final Logger log = LoggerFactory.getLogger(GastosTools.class);

    private final GastoService service;

    public GastosTools(GastoService service) {
        this.service = service;
    }

    @Tool(description = "Registra um novo gasto do usuário. Use quando ele disser que gastou, pagou ou comprou algo.")
    public String registrarGasto(
            @ToolParam(description = "Descrição curta do gasto, por exemplo: almoço, uber, mercado") String descricao,
            @ToolParam(description = "Valor gasto em reais, por exemplo 45.90") Double valor,
            @ToolParam(description = "Categoria: ALIMENTACAO, TRANSPORTE, MORADIA, LAZER, SAUDE, EDUCACAO ou OUTROS") String categoria,
            @ToolParam(description = "Data do gasto no formato AAAA-MM-DD. Se omitida, usa a data de hoje", required = false) String data) {
        log.info("[TOOL] registrarGasto descricao={} valor={} categoria={} data={}", descricao, valor, categoria, data);
        try {
            if (valor == null) {
                throw new IllegalArgumentException("O valor do gasto é obrigatório.");
            }
            LocalDate dataGasto = (data == null || data.isBlank()) ? null : parseData(data, "data");
            Gasto gasto = service.registrar(descricao, BigDecimal.valueOf(valor), categoria, dataGasto);
            return "Gasto registrado: #%d - %s, %s, categoria %s, em %s.".formatted(
                    gasto.getId(), gasto.getDescricao(), brl(gasto.getValor()), gasto.getCategoria(), gasto.getData());
        } catch (IllegalArgumentException e) {
            return "Erro: " + e.getMessage();
        }
    }

    @Tool(description = "Calcula o total gasto em um período, opcionalmente só de uma categoria. "
            + "Use para perguntas como 'quanto gastei este mês?' ou 'quanto gastei com transporte?'.")
    public String totalGasto(
            @ToolParam(description = "Data inicial do período, formato AAAA-MM-DD") String dataInicio,
            @ToolParam(description = "Data final do período, formato AAAA-MM-DD") String dataFim,
            @ToolParam(description = "Categoria para filtrar: ALIMENTACAO, TRANSPORTE, MORADIA, LAZER, SAUDE, EDUCACAO ou OUTROS. Omita para somar tudo", required = false) String categoria) {
        log.info("[TOOL] totalGasto inicio={} fim={} categoria={}", dataInicio, dataFim, categoria);
        try {
            LocalDate inicio = parseData(dataInicio, "dataInicio");
            LocalDate fim = parseData(dataFim, "dataFim");
            BigDecimal total = service.total(inicio, fim, categoria);
            String filtro = (categoria == null || categoria.isBlank())
                    ? "" : " na categoria " + CategoriaGasto.deTexto(categoria);
            return "Total gasto%s de %s a %s: %s.".formatted(filtro, inicio, fim, brl(total));
        } catch (IllegalArgumentException e) {
            return "Erro: " + e.getMessage();
        }
    }

    @Tool(description = "Mostra quanto foi gasto em cada categoria em um período, da maior para a menor. "
            + "Use para perguntas como 'onde mais gastei este mês?'.")
    public String resumoPorCategoria(
            @ToolParam(description = "Data inicial do período, formato AAAA-MM-DD") String dataInicio,
            @ToolParam(description = "Data final do período, formato AAAA-MM-DD") String dataFim) {
        log.info("[TOOL] resumoPorCategoria inicio={} fim={}", dataInicio, dataFim);
        try {
            LocalDate inicio = parseData(dataInicio, "dataInicio");
            LocalDate fim = parseData(dataFim, "dataFim");
            Map<CategoriaGasto, BigDecimal> resumo = service.resumoPorCategoria(inicio, fim);
            if (resumo.isEmpty()) {
                return "Nenhum gasto registrado de %s a %s.".formatted(inicio, fim);
            }
            String linhas = resumo.entrySet().stream()
                    .map(e -> e.getKey() + ": " + brl(e.getValue()))
                    .collect(Collectors.joining("; "));
            return "Gastos por categoria de %s a %s: %s.".formatted(inicio, fim, linhas);
        } catch (IllegalArgumentException e) {
            return "Erro: " + e.getMessage();
        }
    }

    @Tool(description = "Lista os gastos mais recentes registrados pelo usuário.")
    public String ultimosGastos(
            @ToolParam(description = "Quantidade de gastos a listar, de 1 a 20. Padrão: 5", required = false) Integer quantidade) {
        log.info("[TOOL] ultimosGastos quantidade={}", quantidade);
        try {
            List<Gasto> gastos = service.ultimos(quantidade == null ? 5 : quantidade);
            if (gastos.isEmpty()) {
                return "Nenhum gasto registrado ainda.";
            }
            return gastos.stream()
                    .map(g -> "#%d %s - %s (%s) em %s".formatted(
                            g.getId(), g.getDescricao(), brl(g.getValor()), g.getCategoria(), g.getData()))
                    .collect(Collectors.joining("\n"));
        } catch (IllegalArgumentException e) {
            return "Erro: " + e.getMessage();
        }
    }

    private static LocalDate parseData(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("O campo " + campo + " é obrigatório (formato AAAA-MM-DD).");
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida em " + campo + ": use o formato AAAA-MM-DD.");
        }
    }

    private static String brl(BigDecimal valor) {
        return "R$ " + valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
