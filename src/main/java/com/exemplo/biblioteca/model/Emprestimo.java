package com.exemplo.biblioteca.model;

import java.time.LocalDate;

public record Emprestimo(int id, int livroId, int membroId,
                         LocalDate dataEmprestimo, LocalDate dataDevolucao) {

    public boolean devolvido() {
        return dataDevolucao != null;
    }
}
