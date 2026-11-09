package com.exemplo.biblioteca.service;

/** Erro causado por uma regra da biblioteca (e não por falha técnica). */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
