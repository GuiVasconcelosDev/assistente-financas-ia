# Assistente de Finanças por Voz (Spring AI)

API inteligente para **registrar e consultar gastos pessoais falando ou escrevendo**.
Projeto do desafio da DIO com Spring Boot e Spring AI: em vez de evoluir o projeto base,
propus uma ideia nova usando a mesma base técnica (ChatClient, Tool Calling e transcrição de áudio).

> "Gastei 45 reais no almoço ontem"  →  a IA entende a intenção, chama uma função real da
> aplicação, salva no banco e responde ao usuário.

## O que o projeto faz

1. Recebe uma **mensagem de texto** ou um **arquivo de áudio**.
2. Se for áudio, **transcreve para texto** (`TranscriptionModel`).
3. O modelo de linguagem (`ChatClient`) **entende a intenção** e escolhe uma ferramenta (**Tool Calling**).
4. A ferramenta executa um caso de uso real: registrar gasto, somar por período/categoria, resumir ou listar.
5. O modelo gera a **resposta final** em português.

```
Áudio ─► Transcrição ─► ChatClient ─► @Tool ─► GastoService ─► H2
 texto ──────────────────┘                         (validações)
```

## Diferenciais desta versão

- **Validações de negócio antes de salvar**: valor maior que zero e abaixo do limite, descrição obrigatória,
  data não pode ser futura, período válido. Ficam em um único lugar (`GastoService`) e valem tanto para o REST
  quanto para a IA.
- **Erros que a IA consegue explicar**: quando uma regra falha, a ferramenta devolve a mensagem de erro ao
  modelo, que explica o problema ao usuário em vez de quebrar a conversa.
- **Data enviada ao modelo a cada pergunta**: o modelo não sabe que dia é hoje, então "ontem" e "mês passado"
  viram datas reais.
- **Testes que não dependem da OpenAI**: as regras e as ferramentas são testadas sem chave de API.
- **Log de auditoria das ferramentas**: toda chamada de ferramenta aparece no console como `[TOOL] ...`.

## Tecnologias

Java 17 · Spring Boot 3.5 · Spring AI 1.1 (OpenAI) · Spring Data JPA · H2 · Bean Validation · JUnit 5, AssertJ e Mockito

## Arquitetura

```
src/main/java/com/gui/financas/
├── domain/        Gasto, CategoriaGasto, GastoRepository
├── application/   GastoService (casos de uso + validações)
├── ai/            GastosTools (@Tool), AssistenteFinanceiro (ChatClient), TranscricaoService
└── web/           AssistenteController, GastoController, DTOs, ApiExceptionHandler
```

A IA não acessa o banco: ela só chama ferramentas que delegam para `GastoService`,
o mesmo serviço usado pelos endpoints REST.

## Como executar

Pré-requisitos: Java 17+, Maven e uma chave de API da OpenAI (com crédito) para o assistente.

```bash
export OPENAI_API_KEY="sua_chave_aqui"
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Os modelos podem ser trocados por variáveis de ambiente:
`OPENAI_CHAT_MODEL` (padrão `gpt-4o-mini`) e `OPENAI_TRANSCRIPTION_MODEL` (padrão `whisper-1`).
Sem a chave, a aplicação sobe e os testes passam, mas as chamadas ao assistente retornam erro 502.

## Como testar o fluxo principal

**Por texto:**

```bash
curl -X POST http://localhost:8080/assistente/texto \
  -H "Content-Type: application/json" \
  -d '{"mensagem": "Gastei 45 reais no almoço hoje"}'

curl -X POST http://localhost:8080/assistente/texto \
  -H "Content-Type: application/json" \
  -d '{"mensagem": "Quanto gastei este mês?"}'
```

**Por voz** (grave um áudio dizendo, por exemplo, "gastei 30 reais de uber ontem"):

```bash
arecord -f cd -d 5 gasto.wav     # Linux (pacote alsa-utils); ou grave pelo celular (mp3, m4a, wav)

curl -X POST http://localhost:8080/assistente/audio -F "arquivo=@gasto.wav"
```

A resposta traz a `transcricao` (o que a IA ouviu) e a `resposta` final.

**Endpoints REST diretos (sem IA):**

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/gastos` | Registra um gasto |
| GET | `/gastos?quantidade=10` | Últimos gastos |
| GET | `/gastos/total?inicio=2026-10-01&fim=2026-10-31&categoria=LAZER` | Total no período (categoria opcional) |
| GET | `/gastos/resumo?inicio=2026-10-01&fim=2026-10-31` | Total por categoria |

Categorias: `ALIMENTACAO`, `TRANSPORTE`, `MORADIA`, `LAZER`, `SAUDE`, `EDUCACAO`, `OUTROS`.
Console do banco: `http://localhost:8080/h2-console` (JDBC `jdbc:h2:mem:financas`, usuário `sa`).

**Testes automatizados:**

```bash
mvn test
```

## Possíveis evoluções

- Resposta em áudio com `TextToSpeechModel`
- Memória de conversa (várias mensagens seguidas)
- Receitas além de gastos e saldo do mês
- Edição e exclusão de gastos por voz

## O que aprendi

<!-- Escreva aqui, com suas palavras, o que aprendeu no desafio: -->
<!-- - Como o Tool Calling conecta a IA a funções reais -->
<!-- - Por que as validações ficam na camada de aplicação -->
<!-- - Dificuldades que encontrou e como resolveu -->

## Autor

Guilherme Vasconcelos ([@GuiVasconcelosDev](https://github.com/GuiVasconcelosDev))
