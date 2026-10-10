package com.gui.financas.ai;

/** Falha ao conversar com o provedor de IA (chave inválida, rede, limite etc.). */
public class FalhaProvedorIaException extends RuntimeException {

    public FalhaProvedorIaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
