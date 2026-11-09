package com.exemplo.biblioteca.dao;

import com.exemplo.biblioteca.db.ErroDeBanco;
import com.exemplo.biblioteca.model.Membro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class MembroDao {

    private final Connection conexao;

    public MembroDao(Connection conexao) {
        this.conexao = conexao;
    }

    public Membro inserir(String nome, String email) {
        String sql = "INSERT INTO membro (nome, email) VALUES (?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return new Membro(chaves.getInt(1), nome, email);
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao inserir membro.", e);
        }
    }

    public Optional<Membro> buscarPorId(int id) {
        String sql = "SELECT id, nome, email FROM membro WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new Membro(rs.getInt("id"), rs.getString("nome"), rs.getString("email")))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao buscar membro.", e);
        }
    }

    public boolean existeEmail(String email) {
        String sql = "SELECT 1 FROM membro WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao verificar e-mail.", e);
        }
    }
}
