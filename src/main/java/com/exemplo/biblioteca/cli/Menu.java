package com.exemplo.biblioteca.cli;

import com.exemplo.biblioteca.model.EmprestimoAberto;
import com.exemplo.biblioteca.model.Livro;
import com.exemplo.biblioteca.service.BibliotecaService;
import com.exemplo.biblioteca.service.RegraDeNegocioException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BibliotecaService service;
    private final Scanner entrada = new Scanner(System.in);

    public Menu(BibliotecaService service) {
        this.service = service;
    }

    public void executar() {
        boolean rodando = true;
        while (rodando) {
            System.out.println("""

                    ===== Biblioteca =====
                    1) Cadastrar livro
                    2) Cadastrar membro
                    3) Emprestar livro
                    4) Devolver livro
                    5) Listar livros disponíveis
                    6) Listar empréstimos em aberto
                    0) Sair""");
            System.out.print("> ");
            try {
                switch (entrada.nextLine().trim()) {
                    case "1" -> cadastrarLivro();
                    case "2" -> cadastrarMembro();
                    case "3" -> {
                        var e = service.emprestar(lerInt("ID do livro: "), lerInt("ID do membro: "));
                        System.out.println("Empréstimo #" + e.id() + " registrado.");
                    }
                    case "4" -> {
                        service.devolver(lerInt("ID do empréstimo: "));
                        System.out.println("Devolução registrada.");
                    }
                    case "5" -> imprimirLivros(service.listarDisponiveis());
                    case "6" -> imprimirEmprestimos(service.listarEmprestimosAbertos());
                    case "0" -> rodando = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RegraDeNegocioException | IllegalArgumentException e) {
                System.out.println("Atenção: " + e.getMessage());
            }
        }
        System.out.println("Até mais!");
    }

    private void cadastrarLivro() {
        System.out.print("Título: ");
        String titulo = entrada.nextLine();
        System.out.print("Autor: ");
        String autor = entrada.nextLine();
        System.out.print("Ano (vazio para não informar): ");
        String textoAno = entrada.nextLine().trim();
        Integer ano = textoAno.isEmpty() ? null : lerNumero(textoAno);
        Livro livro = service.cadastrarLivro(titulo, autor, ano);
        System.out.println("Livro #" + livro.id() + " cadastrado.");
    }

    private void cadastrarMembro() {
        System.out.print("Nome: ");
        String nome = entrada.nextLine();
        System.out.print("E-mail: ");
        String email = entrada.nextLine();
        var membro = service.cadastrarMembro(nome, email);
        System.out.println("Membro #" + membro.id() + " cadastrado.");
    }

    private void imprimirLivros(List<Livro> livros) {
        if (livros.isEmpty()) {
            System.out.println("Nenhum livro disponível.");
            return;
        }
        for (Livro l : livros) {
            String ano = l.ano() == null ? "s/d" : String.valueOf(l.ano());
            System.out.printf("#%d %s - %s (%s)%n", l.id(), l.titulo(), l.autor(), ano);
        }
    }

    private void imprimirEmprestimos(List<EmprestimoAberto> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhum empréstimo em aberto.");
            return;
        }
        for (EmprestimoAberto e : lista) {
            System.out.printf("Empréstimo #%d: \"%s\" com %s desde %s%n",
                    e.id(), e.livro(), e.membro(), e.dataEmprestimo().format(FORMATO));
        }
    }

    private int lerInt(String mensagem) {
        System.out.print(mensagem);
        return lerNumero(entrada.nextLine().trim());
    }

    private static int lerNumero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Digite um número válido.");
        }
    }
}
