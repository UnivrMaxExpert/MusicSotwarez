package com.dashapp.model;

import com.dashapp.util.DatabaseManager;
import com.dashapp.view.ViewNavigator;

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

    //private List<Integer>

    public List<BranoBean> getBrani() {
        String sql = "SELECT id, utente, titolo, genere, autori, file, anno, concerto FROM brani";
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

                BranoBean brano = new BranoBean(res.getInt("id"), res.getInt("utente"), titolo, genere, file, anno, res.getBoolean("concerto"), autoriStr.split(","));
                brani.add(brano);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null; // oppure Collections.emptyList() se preferisci evitare null
        }

        return brani;
    }

    public List<BranoBean> getBraniCommentati()
    {
        List<BranoBean> braniList = new ArrayList<>();
        String query = "SELECT DISTINCT brani.id, brani.utente, brani.titolo, brani.genere, brani.autori, brani.file, brani.anno, brani.concerto "+
                        "FROM brani INNER JOIN commenti ON commenti.brano = brani.id WHERE commenti.utente = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, ViewNavigator.getUtenteId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BranoBean brano = new BranoBean(rs.getInt("id"),rs.getInt("utente"),rs.getString("titolo"),
                        Genere.valueOf(rs.getString("genere")),rs.getString("file"),rs.getInt("anno"),
                        rs.getBoolean("concerto"), rs.getString("autori").split(","));
                System.out.println("DAO: "+brano.toString());
                braniList.add(brano);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return braniList;

    }

}
