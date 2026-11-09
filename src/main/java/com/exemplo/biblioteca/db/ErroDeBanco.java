package com.exemplo.biblioteca.db;

/** Erro inesperado ao falar com o banco de dados. */
public class ErroDeBanco extends RuntimeException {
    public ErroDeBanco(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
