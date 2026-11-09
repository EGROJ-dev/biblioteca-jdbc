package com.exemplo.biblioteca.service;

import com.exemplo.biblioteca.db.Banco;
import com.exemplo.biblioteca.model.Emprestimo;
import com.exemplo.biblioteca.model.Livro;
import com.exemplo.biblioteca.model.Membro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BibliotecaServiceTest {

    private BibliotecaService service;
    private Membro ana;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        // cada teste usa um banco em memória novo e vazio
        Banco banco = Banco.emMemoria("teste" + UUID.randomUUID().toString().replace("-", ""));
        service = new BibliotecaService(banco, relogio);
        ana = service.cadastrarMembro("Ana", "ana@exemplo.com");
    }

    @Test
    void livroCadastradoApareceComoDisponivel() {
        Livro livro = service.cadastrarLivro("Dom Casmurro", "Machado de Assis", 1899);

        assertEquals(1, service.listarDisponiveis().size());
        assertEquals(livro.id(), service.listarDisponiveis().get(0).id());
    }

    @Test
    void emprestarTiraOLivroDaListaDeDisponiveis() {
        Livro livro = service.cadastrarLivro("Dom Casmurro", "Machado de Assis", 1899);

        Emprestimo emprestimo = service.emprestar(livro.id(), ana.id());

        assertEquals(LocalDate.of(2026, 10, 30), emprestimo.dataEmprestimo());
        assertTrue(service.listarDisponiveis().isEmpty());
        assertEquals(1, service.listarEmprestimosAbertos().size());
        assertEquals("Dom Casmurro", service.listarEmprestimosAbertos().get(0).livro());
        assertEquals("Ana", service.listarEmprestimosAbertos().get(0).membro());
    }

    @Test
    void naoPermiteEmprestarOMesmoLivroDuasVezes() {
        Livro livro = service.cadastrarLivro("Dom Casmurro", "Machado de Assis", 1899);
        Membro bruno = service.cadastrarMembro("Bruno", "bruno@exemplo.com");
        service.emprestar(livro.id(), ana.id());

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> service.emprestar(livro.id(), bruno.id()));
        assertEquals("Esse livro já está emprestado.", erro.getMessage());
    }

    @Test
    void respeitaOLimiteDeEmprestimosPorMembro() {
        for (int i = 1; i <= BibliotecaService.LIMITE_EMPRESTIMOS_POR_MEMBRO; i++) {
            Livro livro = service.cadastrarLivro("Livro " + i, "Autor", 2000);
            service.emprestar(livro.id(), ana.id());
        }
        Livro extra = service.cadastrarLivro("Livro extra", "Autor", 2001);

        assertThrows(RegraDeNegocioException.class, () -> service.emprestar(extra.id(), ana.id()));
        assertEquals(1, service.listarDisponiveis().size());
    }

    @Test
    void devolverLiberaOLivroEPermiteNovoEmprestimo() {
        Livro livro = service.cadastrarLivro("Dom Casmurro", "Machado de Assis", 1899);
        Emprestimo emprestimo = service.emprestar(livro.id(), ana.id());

        service.devolver(emprestimo.id());

        assertEquals(1, service.listarDisponiveis().size());
        assertTrue(service.listarEmprestimosAbertos().isEmpty());
        assertDoesNotThrow(() -> service.emprestar(livro.id(), ana.id()));
    }

    @Test
    void naoPermiteDevolverDuasVezes() {
        Livro livro = service.cadastrarLivro("Dom Casmurro", "Machado de Assis", 1899);
        Emprestimo emprestimo = service.emprestar(livro.id(), ana.id());
        service.devolver(emprestimo.id());

        assertThrows(RegraDeNegocioException.class, () -> service.devolver(emprestimo.id()));
    }

    @Test
    void rejeitaEmailDuplicado() {
        assertThrows(RegraDeNegocioException.class,
                () -> service.cadastrarMembro("Outra Ana", "ANA@exemplo.com"));
    }

    @Test
    void rejeitaLivroEMembroInexistentes() {
        assertThrows(RegraDeNegocioException.class, () -> service.emprestar(99, ana.id()));
        Livro livro = service.cadastrarLivro("Dom Casmurro", "Machado de Assis", 1899);
        assertThrows(RegraDeNegocioException.class, () -> service.emprestar(livro.id(), 99));
    }

    @Test
    void validaDadosDoLivro() {
        assertThrows(RegraDeNegocioException.class, () -> service.cadastrarLivro(" ", "Autor", 2000));
        assertThrows(RegraDeNegocioException.class, () -> service.cadastrarLivro("Título", "", 2000));
        assertThrows(RegraDeNegocioException.class, () -> service.cadastrarLivro("Título", "Autor", 2999));
    }

    @Test
    void livroSemAnoEPermitido() {
        Livro livro = service.cadastrarLivro("Sem data", "Anônimo", null);
        assertNull(service.listarLivros().get(0).ano());
        assertNotNull(livro);
    }
}
