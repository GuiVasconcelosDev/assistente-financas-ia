package com.gui.financas.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Conversa com o modelo de linguagem usando o ChatClient do Spring AI.
 * As ferramentas de GastosTools ficam disponíveis para o modelo chamar quando precisar.
 */
@Service
public class AssistenteFinanceiro {

    private final ChatClient chatClient;
    private final GastosTools tools;
    private final Clock clock;

    public AssistenteFinanceiro(ChatClient.Builder chatClientBuilder, GastosTools tools, Clock clock) {
        this.chatClient = chatClientBuilder.build();
        this.tools = tools;
        this.clock = clock;
    }

    public String responder(String mensagem) {
        try {
            return chatClient.prompt()
                    .system(promptDoSistema())
                    .user(mensagem)
                    .tools(tools)
                    .call()
                    .content();
        } catch (RuntimeException e) {
            throw new FalhaProvedorIaException(
                    "Falha ao consultar o provedor de IA. Verifique a variável OPENAI_API_KEY e sua conexão.", e);
        }
    }

    // O modelo não sabe que dia é hoje, então enviamos a data a cada pergunta.
    private String promptDoSistema() {
        LocalDate hoje = LocalDate.now(clock);
        String diaDaSemana = hoje.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        return """
                Você é um assistente de finanças pessoais que registra e consulta os gastos do usuário.
                Hoje é %s (%s). O mês atual vai de %s até %s.

                Regras:
                - Para registrar um gasto, use SEMPRE a ferramenta registrarGasto.
                - Para falar de valores gastos, use SEMPRE as ferramentas de consulta. Nunca invente números.
                - Se faltar o valor ou a descrição do gasto, pergunte ao usuário antes de registrar.
                - Converta datas relativas (ontem, semana passada, mês passado) para o formato AAAA-MM-DD.
                - Se uma ferramenta devolver um erro, explique o problema ao usuário de forma simples.
                - Responda em português do Brasil, de forma curta e amigável, com valores em reais (R$).
                """.formatted(hoje, diaDaSemana, hoje.withDayOfMonth(1), hoje.withDayOfMonth(hoje.lengthOfMonth()));
    }
}
