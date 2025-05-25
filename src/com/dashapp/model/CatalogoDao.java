package com.dashapp.model;

import com.dashapp.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CatalogoDao
{
    private List<BranoBean> brani;

    public CatalogoDao() {
    }

    public List<BranoBean> getBrani() {
        String sql = "SELECT id, titolo, genere, autori, file, anno FROM brani";
        List<BranoBean> brani = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet res = stmt.executeQuery()) {

            while (res.next()) {
                String titolo = res.getString("titolo");
                String genereStr = res.getString("genere");
                String autoriStr = res.getString("autori");
                String file = res.getString("file");
                int anno = res.getInt("anno");

                Genere genere = Genere.valueOf(genereStr.toUpperCase()); // o .valueOf(genereStr.trim()) se enum è uguale
                String[] autori = autoriStr.split(",");

                BranoBean brano = new BranoBean(titolo, genere, file, anno, autori);
                brano.setId(res.getInt("id"));
                brani.add(brano);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null; // oppure Collections.emptyList() se preferisci evitare null
        }

        return brani;
    }

}
