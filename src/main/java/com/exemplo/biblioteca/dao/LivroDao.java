package com.exemplo.biblioteca.dao;

import com.exemplo.biblioteca.db.ErroDeBanco;
import com.exemplo.biblioteca.model.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LivroDao {

    private final Connection conexao;

    public LivroDao(Connection conexao) {
        this.conexao = conexao;
    }

    public Livro inserir(String titulo, String autor, Integer ano) {
        String sql = "INSERT INTO livro (titulo, autor, ano) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, titulo);
            ps.setString(2, autor);
            if (ano == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, ano);
            }
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return new Livro(chaves.getInt(1), titulo, autor, ano);
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao inserir livro.", e);
        }
    }

    public Optional<Livro> buscarPorId(int id) {
        String sql = "SELECT id, titulo, autor, ano FROM livro WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao buscar livro.", e);
        }
    }

    public List<Livro> listarTodos() {
        return consultar("SELECT id, titulo, autor, ano FROM livro ORDER BY titulo");
    }

    /** Livros que não têm nenhum empréstimo em aberto. */
    public List<Livro> listarDisponiveis() {
        return consultar("""
                SELECT l.id, l.titulo, l.autor, l.ano
                FROM livro l
                WHERE NOT EXISTS (
                    SELECT 1 FROM emprestimo e
                    WHERE e.livro_id = l.id AND e.data_devolucao IS NULL
                )
                ORDER BY l.titulo
                """);
    }

    private List<Livro> consultar(String sql) {
        try (PreparedStatement ps = conexao.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Livro> livros = new ArrayList<>();
            while (rs.next()) {
                livros.add(mapear(rs));
            }
            return livros;
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao listar livros.", e);
        }
    }

    private static Livro mapear(ResultSet rs) throws SQLException {
        int ano = rs.getInt("ano");
        Integer anoOuNulo = rs.wasNull() ? null : ano;
        return new Livro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"), anoOuNulo);
    }
}
