package com.dashapp.model;

import com.dashapp.util.DatabaseManager;
import com.dashapp.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
    /*Permette di prendere la lista delle richieste per registrarsi dal db e gestisce eventuali spostamenti in caso di richiesta approvata da
    * tabella richiesta a quella utenti*/
public class RequestDao {

    public List<UtenteBean> getRichieste() {
        List<UtenteBean> utenti = new ArrayList<>();
        String sql = "SELECT id, username, password FROM richieste WHERE stato = 'IN_ATTESA'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UtenteBean utente = new UtenteBean(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                );
                utenti.add(utente);
            }

        } catch (SQLException e) {
            System.err.println("Errore durante il recupero delle richieste: " + e.getMessage());
            throw new RuntimeException("Impossibile caricare le richieste dal database.", e);
        }

        return utenti;
    }

    /**
     * Approva una richiesta: inserisce l'utente nella tabella utenti e aggiorna lo stato della richiesta.
     */
    public boolean approvaRichiesta(UtenteBean utente) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Inizia transazione

            // 1. Inserisci nella tabella utenti con origine = 'RICHIESTA'
            String insertUserSql = "INSERT INTO utenti (username, password, admin) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {
                insertStmt.setString(1, utente.getUsername());
                insertStmt.setString(2, utente.getPassword());
                insertStmt.setInt(3, 0); // non admin
                insertStmt.executeUpdate();
            }

            // 2. Aggiorna la richiesta impostando stato = 'APPROVATO'
            String updateRequestSql = "UPDATE richieste SET stato = 'APPROVATO' WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateRequestSql)) {
                updateStmt.setInt(1, utente.getId());
                int updated = updateStmt.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Errore durante l'approvazione della richiesta: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Errore durante il rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Rifiuta una richiesta aggiornando il suo stato.
     */
    public boolean rifiutaRichiesta(UtenteBean utente, String motivoRifiuto) {
        String sql = "UPDATE richieste SET stato = 'RIFIUTATO', motivo_rifiuto = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, motivoRifiuto);
            stmt.setInt(2, utente.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante il rifiuto della richiesta per: " + utente.getUsername());
            e.printStackTrace();
            return false;
        }
    }

    public String getStatoRichiesta(String username) {
        String sql = "SELECT stato FROM richieste WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("stato");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean trasferisciUtenteDaRichiestaAUtenti(String username, String inputPassword) {
        String selectSql = "SELECT password FROM richieste WHERE username = ?";
        String insertSql = "INSERT INTO utenti (username, password, admin) VALUES (?, ?, 0)";
        String deleteSql = "DELETE FROM richieste WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            String hashedPassword = null;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, username);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    hashedPassword = rs.getString("password");
                } else {
                    return false;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, username);
                deleteStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void aggiornaStatoRichiesta(String username, String nuovoStato) {
        String sql = "UPDATE richieste SET stato = ? WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuovoStato);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
