package com.dashapp.model;

import com.dashapp.util.DatabaseManager;
import com.dashapp.util.PasswordHasher;
import com.dashapp.view.ViewNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccessoDao
{
    public AccessoDao() throws SQLException
    {}

    public boolean loginControllo(UtenteBean utente)
    {
        //aggiungere nella query anche admin così successivamente, nel controller, si fa un if con cui si controlla se è admin oppure no e si ridireziona nel fxml corretto
        String sql = "SELECT id, password FROM utenti WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utente.getUsername());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                ViewNavigator.setUtenteId(rs.getInt("id"));
                // Verifica la password usando PasswordHasher
                return PasswordHasher.checkPassword(utente.getPassword(), hashedPassword);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean registrazioneControllo(UtenteBean utente) {
        if(loginControllo(utente))
            return false;
        else
        {
            String sql = "INSERT INTO utenti (username, password, admin) VALUES (?, ?, ?)";//Deve fare la query alla tabella richieste DA MODIFICARE
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                String hashedPassword = PasswordHasher.hashPassword(utente.getPassword());

                stmt.setString(1, utente.getUsername());
                stmt.setString(2, hashedPassword); // Salva l'hash, non la password in chiaro
                stmt.setBoolean(3, false);

                return stmt.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
