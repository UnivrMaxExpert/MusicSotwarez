package com.dashapp.model;

import com.dashapp.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
    /*Permette di prendere la lista degli utenti e del loro id nel db*/
public class VisualizzaDao
{
    private Map<Integer, String> utenti = new HashMap<>();

    public VisualizzaDao() {
    }

    public boolean carica()
    {
        String sql = "SELECT id, username FROM utenti";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                utenti.put(rs.getInt("id"), rs.getString("username"));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // oppure Collections.emptyList();
        }
    }

    public String getUtente(int id)
    {
        return utenti.get(id);
    }
}
