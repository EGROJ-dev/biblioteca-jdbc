package com.exemplo.biblioteca.dao;

import com.exemplo.biblioteca.db.ErroDeBanco;
import com.exemplo.biblioteca.model.Emprestimo;
import com.exemplo.biblioteca.model.EmprestimoAberto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmprestimoDao {

    private final Connection conexao;

    public EmprestimoDao(Connection conexao) {
        this.conexao = conexao;
    }

    public Emprestimo inserir(int livroId, int membroId, LocalDate data) {
        String sql = "INSERT INTO emprestimo (livro_id, membro_id, data_emprestimo) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, livroId);
            ps.setInt(2, membroId);
            ps.setObject(3, data);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return new Emprestimo(chaves.getInt(1), livroId, membroId, data, null);
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao registrar empréstimo.", e);
        }
    }

    public Optional<Emprestimo> buscarPorId(int id) {
        String sql = "SELECT id, livro_id, membro_id, data_emprestimo, data_devolucao FROM emprestimo WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Emprestimo(
                        rs.getInt("id"), rs.getInt("livro_id"), rs.getInt("membro_id"),
                        rs.getObject("data_emprestimo", LocalDate.class),
                        rs.getObject("data_devolucao", LocalDate.class)));
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao buscar empréstimo.", e);
        }
    }

    public void marcarDevolvido(int id, LocalDate data) {
        String sql = "UPDATE emprestimo SET data_devolucao = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setObject(1, data);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao registrar devolução.", e);
        }
    }

    public boolean livroEstaEmprestado(int livroId) {
        String sql = "SELECT 1 FROM emprestimo WHERE livro_id = ? AND data_devolucao IS NULL";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, livroId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao verificar empréstimo.", e);
        }
    }

    public int contarAbertosDoMembro(int membroId) {
        String sql = "SELECT COUNT(*) FROM emprestimo WHERE membro_id = ? AND data_devolucao IS NULL";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, membroId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao contar empréstimos.", e);
        }
    }

    /** Usa JOIN para trazer o título do livro e o nome do membro junto com o empréstimo. */
    public List<EmprestimoAberto> listarAbertos() {
        String sql = """
                SELECT e.id, l.titulo, m.nome, e.data_emprestimo
                FROM emprestimo e
                JOIN livro  l ON l.id = e.livro_id
                JOIN membro m ON m.id = e.membro_id
                WHERE e.data_devolucao IS NULL
                ORDER BY e.data_emprestimo, e.id
                """;
        try (PreparedStatement ps = conexao.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<EmprestimoAberto> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new EmprestimoAberto(rs.getInt("id"), rs.getString("titulo"),
                        rs.getString("nome"), rs.getObject("data_emprestimo", LocalDate.class)));
            }
            return lista;
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao listar empréstimos.", e);
        }
    }
}
