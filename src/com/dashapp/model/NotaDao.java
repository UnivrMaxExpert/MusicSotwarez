package com.dashapp.model;

import com.dashapp.util.DatabaseManager;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class NotaDao {
    public List<NotaBean> getNotePerBrano(int idBrano) {
        List<NotaBean> note = new ArrayList<>();

        String query = "SELECT * FROM note WHERE brano = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idBrano);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                NotaBean nota = new NotaBean();
                nota.setId(rs.getInt("id"));
                nota.setIdBrano(rs.getInt("brano"));
                nota.setIdUtente(rs.getInt("utente"));
                nota.setDurata(rs.getTime("durata"));
                nota.setDataRegistrazione(rs.getDate("data_registrazione"));
                nota.setLuogoRegistrazione(rs.getString("luogo_registrazione"));
                nota.setSegmentoInizio(rs.getTime("segmento_inizio"));
                nota.setSegmentoFine(rs.getTime("segmento_fine"));
                nota.setTestoLibero(rs.getString("testo_libero"));

                // Recupera strumenti ed esecutori collegati
                nota.setStrumenti(getStrumentiPerNota(nota.getId(), conn));
                nota.setEsecutori(getEsecutoriPerNota(nota.getId(), conn));

                note.add(nota);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return note;
    }

    private List<String> getStrumentiPerNota(int idNota, Connection conn) throws SQLException {
        List<String> strumenti = new ArrayList<>();
        String query = "SELECT s.nome FROM strumenti s JOIN note_strumenti ns ON s.id = ns.strumento WHERE ns.nota = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idNota);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                strumenti.add(rs.getString("nome"));
            }
        }
        return strumenti;
    }

    private List<String> getEsecutoriPerNota(int idNota, Connection conn) throws SQLException {
        List<String> esecutori = new ArrayList<>();
        String query = "SELECT e.nome FROM esecutori e JOIN note_esecutori ne ON e.id = ne.esecutore WHERE ne.nota = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idNota);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                esecutori.add(rs.getString("nome"));
            }
        }
        return esecutori;
    }

    public int inserisciNota(NotaBean nota) {
        String query = "INSERT INTO note (brano, utente, durata, data_registrazione, luogo_registrazione, segmento_inizio, segmento_fine, testo_libero) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, nota.getIdBrano());
            stmt.setInt(2, nota.getIdUtente());
            stmt.setString(3, nota.getDurata().toString());

            if (nota.getDataRegistrazione() != null) {
                stmt.setDate(4, Date.valueOf(nota.getDataRegistrazione().toLocalDate()));
            } else {
                stmt.setDate(4, null);
            }

            stmt.setString(5, nota.getLuogoRegistrazione());
            stmt.setString(6, nota.getSegmentoInizio().toString());
            stmt.setString(7, nota.getSegmentoFine().toString());
            stmt.setString(8, nota.getTestoLibero());

            stmt.executeUpdate();

            // Ottieni l'ID generato
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // ID nota generato
                } else {
                    throw new SQLException("Inserimento nota fallito, nessun ID ottenuto.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Errore durante l'inserimento della nota nel database.");
            return -1; // oppure lancia un'eccezione
        }
    }


    public boolean inserisciStrumenti(boolean flag, int nota, List<Integer> lista) {
        String table = flag ? "note_strumenti" : "note_esecutori";
        String column = flag ? "strumento" : "esecutore";
        String query = "INSERT INTO " + table + " (nota, " + column + ") VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (Integer id : lista) {
                stmt.setInt(1, nota);
                stmt.setInt(2, id);
                stmt.addBatch();
            }

            stmt.executeBatch();
            return true;

        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento in " + (flag ? "note_strumenti" : "note_esecutori"));
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public Map<Integer, String> getOthers(boolean flag) {
        Map<Integer, String> strumenti = new HashMap<>();
        String tableName = flag ? "strumenti" : "esecutori";
        String query = "SELECT id, nome FROM " + tableName;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                strumenti.put(rs.getInt("id"), rs.getString("nome"));
            }

        } catch (SQLException e) {
            System.err.println("Errore nel recupero da " + tableName + ": " + e.getMessage());
            throw new RuntimeException(e);
        }

        return strumenti;
    }

    public boolean addOthers(boolean flag, String other) {
        String tableName = flag ? "strumenti" : "esecutori";
        String query = "INSERT INTO " + tableName + " (nome) VALUES (?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, other);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento in " + tableName + ": " + e.getMessage());
            return false;
        }
    }

}

