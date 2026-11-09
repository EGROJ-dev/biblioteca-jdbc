package com.exemplo.biblioteca.model;

import java.time.LocalDate;

/** Resultado de um JOIN entre emprestimo, livro e membro. */
public record EmprestimoAberto(int id, String livro, String membro, LocalDate dataEmprestimo) {}
