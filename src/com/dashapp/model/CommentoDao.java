package com.dashapp.model;

import com.dashapp.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
    /*Permette di caricare/prendere i commenti salvati nel db*/
public class CommentoDao {

    public void aggiungiCommento(int branoId, int autoreId, String testo, Integer parentId) throws SQLException {
        String sql = "INSERT INTO commenti (brano, parent, utente, testo) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            System.out.println("Brano: " + branoId);
            stmt.setInt(1, branoId);
            if (parentId != null)
                stmt.setInt(2, parentId);
            else
                stmt.setNull(2, Types.INTEGER);
            stmt.setInt(3, autoreId);
            stmt.setString(4, testo);
            stmt.executeUpdate();
        }
    }

    public List<CommentoBean> getCommentiByBranoId(int branoId) throws SQLException {
        String sql = """
            SELECT c.id, c.utente, c.parent, c.brano, c.testo, c.data_creazione, u.username
            FROM commenti c JOIN utenti u ON c.utente = u.id
            WHERE c.brano = ?
            ORDER BY c.data_creazione ASC
        """;
        List<CommentoBean> all = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CommentoBean c = new CommentoBean();
                c.setId(rs.getInt("id"));
                c.setBranoId(rs.getInt("brano"));

                int parentId = rs.getInt("parent");
                if (rs.wasNull()) {
                    c.setParentId(null);
                } else {
                    c.setParentId(parentId);
                }
                c.setAutoreId(rs.getInt("utente"));
                c.setAutore(rs.getString("username"));
                c.setTesto(rs.getString("testo"));
                c.setData(rs.getTimestamp("data_creazione").toLocalDateTime());
                all.add(c);
            }
        }
        return all;
    }

    public int countRepliesByCommentId(int commentId) throws SQLException {
        String query = "SELECT COUNT(*) FROM commento WHERE parent_id = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(query)) {
            ps.setInt(1, commentId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}
