package com.dashapp.model;

import com.dashapp.util.AlertManager;
import com.dashapp.util.DatabaseManager;
import com.dashapp.view.ViewNavigator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/*Permette di prendere la lista dei brani nel db e dei brani che sono commentati*/
public class CatalogoDao
{
    private List<BranoBean> brani;

    public CatalogoDao() {
    }

    //private List<Integer>

    public Map<String, List<String>> getDizionarioAutori() {
        Map<String, List<String>> dizionario = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();) {
            String query = """
            SELECT b.titolo AS brano, e.nome AS artista, e.ruolo
            FROM brani b
            JOIN brani_artisti ba ON b.id = ba.brano
            JOIN esecutori e ON ba.artista = e.id
            WHERE e.ruolo = 'AUTORE' OR e.ruolo = 'ENTRAMBI'
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String brano = rs.getString("brano");
                    String artista = rs.getString("artista");

                    dizionario.computeIfAbsent(brano, k -> new ArrayList<>()).add(artista);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dizionario;
    }

    public Map<String, List<String>> getDizionarioEsecutori() {
        Map<String, List<String>> dizionario = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();) {

            // 1. Brani ↔ Esecutori
            String q1 = """
            SELECT b.titolo AS brano, e.nome AS esecutore
            FROM brani b
            JOIN brani_artisti ba ON b.id = ba.brano
            JOIN esecutori e ON ba.artista = e.id
            WHERE e.ruolo = 'ESECUTORE' OR e.ruolo = 'ENTRAMBI'
        """;

            try (PreparedStatement stmt = conn.prepareStatement(q1);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String brano = rs.getString("brano");
                    String esecutore = rs.getString("esecutore");

                    dizionario.computeIfAbsent(brano, k -> new ArrayList<>()).add(esecutore);
                }
            }

            // 2. Meta ↔ Esecutori ↔ Brano
            String q2 = """
            SELECT b.titolo AS brano, e.nome AS esecutore
            FROM meta m
            JOIN meta_esecutori me ON m.id = me.meta
            JOIN esecutori e ON me.esecutore = e.id
            JOIN brani b ON m.brano = b.id
            WHERE e.ruolo = 'ESECUTORE' OR e.ruolo = 'ENTRAMBI'
        """;

            try (PreparedStatement stmt = conn.prepareStatement(q2);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String brano = rs.getString("brano");
                    String esecutore = rs.getString("esecutore");

                    dizionario.computeIfAbsent(brano, k -> new ArrayList<>()).add(esecutore);
                }
            }

            // 3. Note ↔ Esecutori ↔ Brano
            String q3 = """
            SELECT b.titolo AS brano, e.nome AS esecutore
            FROM note n
            JOIN note_esecutori ne ON n.id = ne.nota
            JOIN esecutori e ON ne.esecutore = e.id
            JOIN brani b ON n.brano = b.id
            WHERE e.ruolo = 'ESECUTORE' OR e.ruolo = 'ENTRAMBI'
        """;

            try (PreparedStatement stmt = conn.prepareStatement(q3);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String brano = rs.getString("brano");
                    String esecutore = rs.getString("esecutore");

                    dizionario.computeIfAbsent(brano, k -> new ArrayList<>()).add(esecutore);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Rimuove duplicati all’interno di ogni lista
        dizionario.replaceAll((k, v) -> v.stream().distinct().toList());

        return dizionario;
    }

    public Map<String, List<String>> getDizionarioStrumenti() {
        Map<String, Set<String>> branoStrumentiMap = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();) {

            // 1. brani_strumenti
            String query1 = "SELECT b.titolo, s.nome FROM brani_strumenti bs " +
                    "JOIN brani b ON bs.brano = b.id " +
                    "JOIN strumenti s ON bs.strumento = s.id";
            try (PreparedStatement stmt = conn.prepareStatement(query1);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    branoStrumentiMap
                            .computeIfAbsent(rs.getString("titolo"), k -> new HashSet<>())
                            .add(rs.getString("nome"));
                }
            }

            // 2. meta → meta_strumenti
            String query2 = "SELECT b.titolo, s.nome FROM meta m " +
                    "JOIN meta_strumenti ms ON m.id = ms.meta " +
                    "JOIN strumenti s ON ms.strumento = s.id " +
                    "JOIN brani b ON m.brano = b.id";
            try (PreparedStatement stmt = conn.prepareStatement(query2);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    branoStrumentiMap
                            .computeIfAbsent(rs.getString("titolo"), k -> new HashSet<>())
                            .add(rs.getString("nome"));
                }
            }

            // 3. note → note_strumenti
            String query3 = "SELECT b.titolo, s.nome FROM note n " +
                    "JOIN note_strumenti ns ON n.id = ns.nota " +
                    "JOIN strumenti s ON ns.strumento = s.id " +
                    "JOIN brani b ON n.brano = b.id";
            try (PreparedStatement stmt = conn.prepareStatement(query3);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    branoStrumentiMap
                            .computeIfAbsent(rs.getString("titolo"), k -> new HashSet<>())
                            .add(rs.getString("nome"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Converti Set a List per compatibilità
        return branoStrumentiMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ArrayList<>(e.getValue())
                ));
    }



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
                Integer anno = res.getInt("anno");
                if (res.wasNull())
                    anno = null;
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
