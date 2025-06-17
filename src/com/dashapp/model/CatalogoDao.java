package com.dashapp.model;

import com.dashapp.util.DatabaseManager;
import com.dashapp.view.ViewNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
    /*Permette di prendere la lista dei brani nel db e dei brani che sono commentati*/
public class CatalogoDao
{
    private List<BranoBean> brani;

    public CatalogoDao() {
    }

    //private List<Integer>

    public List<BranoBean> getBrani() {
        String sql = "SELECT id, utente, titolo, genere, file, anno, concerto FROM brani";
        String sqlAutori = """
        SELECT e.nome
        FROM esecutori e
        JOIN brani_artisti ba ON e.id = ba.artista
        WHERE ba.brano = ? AND e.ruolo IN ('autore', 'entrambi')
    """;

        List<BranoBean> brani = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet res = stmt.executeQuery();
             PreparedStatement stmtAutori = conn.prepareStatement(sqlAutori)) {

            while (res.next()) {
                int branoId = res.getInt("id");
                String titolo = res.getString("titolo");
                String genereStr = res.getString("genere");
                String file = res.getString("file");
                int anno = res.getInt("anno");
                boolean concerto = res.getBoolean("concerto");
                int utenteId = res.getInt("utente");

                Genere genere = Genere.valueOf(genereStr.toUpperCase());

                // Ottenere autori per questo brano
                List<String> autori = new ArrayList<>();
                stmtAutori.setInt(1, branoId);
                try (ResultSet autoriRes = stmtAutori.executeQuery()) {
                    while (autoriRes.next()) {
                        autori.add(autoriRes.getString("nome"));
                    }
                }

                String[] autoriArray = autori.toArray(new String[0]);

                BranoBean brano = new BranoBean(branoId, utenteId, titolo, genere, file, anno, concerto, autoriArray);
                brani.add(brano);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null; // oppure Collections.emptyList();
        }

        return brani;
    }


    public List<BranoBean> getBraniCommentati() {
        List<BranoBean> braniList = new ArrayList<>();

        String queryBrani = """
        SELECT DISTINCT brani.id, brani.utente, brani.titolo, brani.genere,
                        brani.file, brani.anno, brani.concerto
        FROM brani
        INNER JOIN commenti ON commenti.brano = brani.id
        WHERE commenti.utente = ?
    """;

        String queryAutori = """
        SELECT e.nome
        FROM esecutori e
        JOIN brani_artisti ba ON e.id = ba.artista
        WHERE ba.brano = ? AND e.ruolo IN ('autore', 'entrambi')
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmtBrani = conn.prepareStatement(queryBrani);
             PreparedStatement stmtAutori = conn.prepareStatement(queryAutori)) {

            stmtBrani.setInt(1, ViewNavigator.getUtenteId());
            ResultSet rsBrani = stmtBrani.executeQuery();

            while (rsBrani.next()) {
                int branoId = rsBrani.getInt("id");
                int utenteId = rsBrani.getInt("utente");
                String titolo = rsBrani.getString("titolo");
                Genere genere = Genere.valueOf(rsBrani.getString("genere").toUpperCase());
                String file = rsBrani.getString("file");
                int anno = rsBrani.getInt("anno");
                boolean concerto = rsBrani.getBoolean("concerto");

                // Recupera autori per questo brano
                List<String> autori = new ArrayList<>();
                stmtAutori.setInt(1, branoId);
                try (ResultSet rsAutori = stmtAutori.executeQuery()) {
                    while (rsAutori.next()) {
                        autori.add(rsAutori.getString("nome"));
                    }
                }

                String[] autoriArray = autori.toArray(new String[0]);

                BranoBean brano = new BranoBean(branoId, utenteId, titolo, genere, file, anno, concerto, autoriArray);
                System.out.println("DAO: " + brano);
                braniList.add(brano);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return braniList;
    }


}
