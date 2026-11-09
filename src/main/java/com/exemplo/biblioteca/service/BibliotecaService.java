package com.exemplo.biblioteca.service;

import com.exemplo.biblioteca.dao.EmprestimoDao;
import com.exemplo.biblioteca.dao.LivroDao;
import com.exemplo.biblioteca.dao.MembroDao;
import com.exemplo.biblioteca.db.Banco;
import com.exemplo.biblioteca.db.ErroDeBanco;
import com.exemplo.biblioteca.model.Emprestimo;
import com.exemplo.biblioteca.model.EmprestimoAberto;
import com.exemplo.biblioteca.model.Livro;
import com.exemplo.biblioteca.model.Membro;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class BibliotecaService {

    public static final int LIMITE_EMPRESTIMOS_POR_MEMBRO = 3;

    private final Banco banco;
    private final Clock relogio;

    public BibliotecaService(Banco banco, Clock relogio) {
        this.banco = banco;
        this.relogio = relogio;
    }

    public Livro cadastrarLivro(String titulo, String autor, Integer ano) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraDeNegocioException("O título não pode ser vazio.");
        }
        if (autor == null || autor.isBlank()) {
            throw new RegraDeNegocioException("O autor não pode ser vazio.");
        }
        if (ano != null && (ano < 0 || ano > LocalDate.now(relogio).getYear())) {
            throw new RegraDeNegocioException("Ano de publicação inválido.");
        }
        return emTransacao(c -> new LivroDao(c).inserir(titulo.trim(), autor.trim(), ano));
    }

    public Membro cadastrarMembro(String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O nome não pode ser vazio.");
        }
        if (email == null || !email.contains("@")) {
            throw new RegraDeNegocioException("E-mail inválido.");
        }
        return emTransacao(c -> {
            MembroDao membros = new MembroDao(c);
            if (membros.existeEmail(email.trim())) {
                throw new RegraDeNegocioException("Já existe um membro com esse e-mail.");
            }
            return membros.inserir(nome.trim(), email.trim());
        });
    }

    /**
     * Todas as verificações e o INSERT acontecem na mesma transação:
     * ou o empréstimo é registrado por inteiro, ou nada é gravado.
     */
    public Emprestimo emprestar(int livroId, int membroId) {
        return emTransacao(c -> {
            LivroDao livros = new LivroDao(c);
            MembroDao membros = new MembroDao(c);
            EmprestimoDao emprestimos = new EmprestimoDao(c);

            livros.buscarPorId(livroId)
                    .orElseThrow(() -> new RegraDeNegocioException("Livro não encontrado."));
            membros.buscarPorId(membroId)
                    .orElseThrow(() -> new RegraDeNegocioException("Membro não encontrado."));

            if (emprestimos.livroEstaEmprestado(livroId)) {
                throw new RegraDeNegocioException("Esse livro já está emprestado.");
            }
            if (emprestimos.contarAbertosDoMembro(membroId) >= LIMITE_EMPRESTIMOS_POR_MEMBRO) {
                throw new RegraDeNegocioException(
                        "O membro já atingiu o limite de " + LIMITE_EMPRESTIMOS_POR_MEMBRO + " empréstimos.");
            }
            return emprestimos.inserir(livroId, membroId, LocalDate.now(relogio));
        });
    }

    public void devolver(int emprestimoId) {
        emTransacao(c -> {
            EmprestimoDao emprestimos = new EmprestimoDao(c);
            Emprestimo emprestimo = emprestimos.buscarPorId(emprestimoId)
                    .orElseThrow(() -> new RegraDeNegocioException("Empréstimo não encontrado."));
            if (emprestimo.devolvido()) {
                throw new RegraDeNegocioException("Esse empréstimo já foi devolvido.");
            }
            emprestimos.marcarDevolvido(emprestimoId, LocalDate.now(relogio));
            return null;
        });
    }

    public List<Livro> listarLivros() {
        return emTransacao(c -> new LivroDao(c).listarTodos());
    }

    public List<Livro> listarDisponiveis() {
        return emTransacao(c -> new LivroDao(c).listarDisponiveis());
    }

    public List<EmprestimoAberto> listarEmprestimosAbertos() {
        return emTransacao(c -> new EmprestimoDao(c).listarAbertos());
    }

    private <T> T emTransacao(Function<Connection, T> acao) {
        try (Connection conexao = banco.abrir()) {
            conexao.setAutoCommit(false);
            try {
                T resultado = acao.apply(conexao);
                conexao.commit();
                return resultado;
            } catch (RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro de transação.", e);
        }
    }
}
