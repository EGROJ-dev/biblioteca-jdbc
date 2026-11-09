# 📚 Biblioteca JDBC

![CI](https://github.com/EGROJ-dev/biblioteca-jdbc/actions/workflows/ci.yml/badge.svg)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Licença MIT](https://img.shields.io/badge/licença-MIT-blue)

Sistema de biblioteca em linha de comando feito com **Java 17** e **JDBC**, guardando os dados em um banco **H2** (SQL). Permite cadastrar livros e membros, emprestar, devolver e consultar o que está disponível.

É um projeto de estudo para praticar **acesso a banco de dados em Java sem frameworks**: SQL escrito à mão, o padrão DAO e transações.

## Objetivo

Praticar o acesso a banco de dados em Java **sem frameworks**, entendendo na prática como SQL, JDBC, o padrão DAO e as transações se encaixam em um sistema com regras de negócio reais (o empréstimo de livros).

## Funcionalidades

- Cadastro de livros e de membros (e-mail único)
- Empréstimo e devolução de livros
- Listagem de livros disponíveis e de empréstimos em aberto
- Regras de negócio: um livro só pode estar com uma pessoa por vez, e cada membro pode ter até 3 empréstimos ao mesmo tempo

## Tecnologias

| Tecnologia | Para que serve no projeto |
|---|---|
| Java 17 | Linguagem do projeto |
| JDBC | Comunicação do Java com o banco de dados |
| H2 | Banco de dados SQL que roda dentro do próprio programa, sem instalação |
| Maven | Compilação e gerenciamento de dependências |
| JUnit 5 | Testes automatizados |
| GitHub Actions | Integração contínua: roda os testes a cada alteração |

## Conceitos praticados

| Conceito | Onde ver |
|---|---|
| JDBC com `PreparedStatement` (protege contra SQL injection) | `dao/` |
| Padrão **DAO** (acesso ao banco separado das regras) | `dao/` e `service/` |
| **Transações** (`commit` e `rollback`) | `BibliotecaService.emTransacao` |
| `JOIN` entre três tabelas | `EmprestimoDao.listarAbertos` |
| `NOT EXISTS` (subconsulta) | `LivroDao.listarDisponiveis` |
| Chaves estrangeiras, `UNIQUE` e `NOT NULL` | `src/main/resources/schema.sql` |
| Testes com banco em memória | `BibliotecaServiceTest` |

## Estrutura

```
src/main/java/com/exemplo/biblioteca
├── Main.java
├── cli/Menu.java               # menu no terminal
├── db/                         # conexão com o H2 e criação das tabelas
├── model/                      # Livro, Membro, Emprestimo (records)
├── dao/                        # SQL de cada tabela
└── service/BibliotecaService   # regras de negócio e transações
src/main/resources/schema.sql   # definição das tabelas
```

## Como executar

Requisitos: JDK 17+ e Maven 3.8+.

```bash
git clone https://github.com/EGROJ-dev/biblioteca-jdbc.git
cd biblioteca-jdbc
mvn clean package
java -jar target/biblioteca-jdbc-1.0.0.jar
```

Os dados ficam na pasta `dados/`. Para usar outra pasta: `java -jar target/biblioteca-jdbc-1.0.0.jar minha-pasta`.

> Se os acentos aparecerem trocados no terminal do Windows, use o Git Bash.

## Exemplo de uso

```
Livro #1 cadastrado.
Membro #1 cadastrado.
Empréstimo #1 registrado.
Atenção: Esse livro já está emprestado.
Empréstimo #1: "Dom Casmurro" com Ana desde 08/11/2026
Devolução registrada.
```

## Testes

```bash
mvn test
```

São 10 testes, todos com um banco H2 em memória que nasce vazio a cada teste.

## Próximos passos

- [ ] Multa por atraso na devolução
- [ ] Busca de livros por título ou autor
- [ ] Trocar o H2 pelo MySQL
- [ ] Transformar em uma API REST

## Autor

**Jorge Gabriel Cordeiro do Nascimento**
[LinkedIn](https://www.linkedin.com/in/jorge-gabriel-cordeiro-9b25702b) · [GitHub](https://github.com/EGROJ-dev)

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
