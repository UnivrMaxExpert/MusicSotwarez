package com.dashapp.model;

import com.dashapp.util.DatabaseManager;

import java.sql.*;
import java.sql.Date;
import java.util.*;
    /*Permette di caricare/prendere note dal db*/
public class NotaDao {
    private Connection conn;
    private String query;
    private PreparedStatement stmt;
    private ResultSet rs;

    // Recupera le note per un brano
    public List<NotaBean> getNotePerBrano(int idBrano) {
        List<NotaBean> note = new ArrayList<>();
        query = "SELECT * FROM note WHERE brano = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idBrano);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaBean nota = populateNotaBean(rs);
                    nota.setStrumenti(getStrumentiPerNota(nota.getId(), conn));
                    nota.setEsecutori(getEsecutoriPerNota(nota.getId(), conn));
                    note.add(nota);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle note: " + e.getMessage(), e);
        }
        return note;
    }

    // Popola un NotaBean da ResultSet
    private NotaBean populateNotaBean(ResultSet rs) throws SQLException {
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
        return nota;
    }

    private List<String> getStrumentiPerNota(int idNota, Connection conn) throws SQLException {
        query = "SELECT s.nome FROM strumenti s JOIN note_strumenti ns ON s.id = ns.strumento WHERE ns.nota = ?";
        List<String> strumenti = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    strumenti.add(rs.getString("nome"));
                }
            }
        }
        return strumenti;
    }

    private List<String> getEsecutoriPerNota(int idNota, Connection conn) throws SQLException {
        String query = "SELECT e.nome FROM esecutori e JOIN note_esecutori ne ON e.id = ne.esecutore WHERE ne.nota = ?";
        List<String> esecutori = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idNota);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    esecutori.add(rs.getString("nome"));
                }
            }
        }
        return esecutori;
    }

    public Map<Integer, String> getUtenti()
    {
        String query = "SELECT id, username FROM utenti";
        Map<Integer, String> utenti = new HashMap<>();
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next())
                    utenti.put(rs.getInt("id"), rs.getString("username"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return utenti;
    }

    // Inserisce una nota
    public int inserisciNota(NotaBean nota) {
        String query = """
            INSERT INTO note 
            (brano, utente, durata, data_registrazione, luogo_registrazione, segmento_inizio, segmento_fine, testo_libero)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, nota.getIdBrano());
            stmt.setInt(2, nota.getIdUtente());
            stmt.setString(3, nota.getDurata().toString());
            stmt.setDate(4, nota.getDataRegistrazione() != null ? Date.valueOf(nota.getDataRegistrazione().toLocalDate()) : null);
            stmt.setString(5, nota.getLuogoRegistrazione());
            stmt.setString(6, nota.getSegmentoInizio().toString());
            stmt.setString(7, nota.getSegmentoFine().toString());
            stmt.setString(8, nota.getTestoLibero());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Inserimento nota fallito, nessun ID ottenuto.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento della nota: " + e.getMessage(), e);
        }
    }

    public int inserisciMeta(MetaBean meta) {
        String query = """
        INSERT INTO meta 
        (brano, utente, titolo_brano, inizio, fine, commentoBean, data_inserimento)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, meta.getIdBrano());
            stmt.setInt(2, meta.getIdUtente());
            stmt.setString(3, meta.getTitolo());
            stmt.setTime(4, meta.getSegmentoInizio());
            stmt.setTime(5, meta.getSegmentoFine());
            stmt.setString(6, meta.getCommento());
            stmt.setDate(7, meta.getDataRegistrazione() != null ?
                    Date.valueOf(meta.getDataRegistrazione().toLocalDate()) : null);

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idMeta = generatedKeys.getInt(1);

                    // Inserisci strumenti collegati
                    if (meta.getStrumenti() != null && !meta.getStrumenti().isEmpty()) {
                        String selectStrumento = "SELECT id FROM strumenti WHERE nome = ?";
                        String insertStrumento = "INSERT INTO meta_strumenti (meta, strumento) VALUES (?, ?)";
                        try (PreparedStatement stmtSelect = conn.prepareStatement(selectStrumento);
                             PreparedStatement stmtInsert = conn.prepareStatement(insertStrumento)) {

                            for (String nomeStrumento : meta.getStrumenti()) {
                                stmtSelect.setString(1, nomeStrumento);
                                try (ResultSet rs = stmtSelect.executeQuery()) {
                                    if (rs.next()) {
                                        int idStrumento = rs.getInt("id");
                                        stmtInsert.setInt(1, idMeta);
                                        stmtInsert.setInt(2, idStrumento);
                                        stmtInsert.addBatch();
                                    } else {
                                        System.err.println("Strumento non trovato: " + nomeStrumento);
                                    }
                                }
                            }
                            stmtInsert.executeBatch();
                        }
                    }

                    // Inserisci esecutori collegati
                    if (meta.getEsecutori() != null && !meta.getEsecutori().isEmpty()) {
                        String selectEsecutore = "SELECT id FROM esecutori WHERE nome = ?";
                        String insertEsecutore = "INSERT INTO meta_esecutori (meta, esecutore) VALUES (?, ?)";
                        try (PreparedStatement stmtSelect = conn.prepareStatement(selectEsecutore);
                             PreparedStatement stmtInsert = conn.prepareStatement(insertEsecutore)) {

                            for (String nomeEsecutore : meta.getEsecutori()) {
                                stmtSelect.setString(1, nomeEsecutore);
                                try (ResultSet rs = stmtSelect.executeQuery()) {
                                    if (rs.next()) {
                                        int idEsecutore = rs.getInt("id");
                                        stmtInsert.setInt(1, idMeta);
                                        stmtInsert.setInt(2, idEsecutore);
                                        stmtInsert.addBatch();
                                    } else {
                                        System.err.println("Esecutore non trovato: " + nomeEsecutore);
                                    }
                                }
                            }
                            stmtInsert.executeBatch();
                        }
                    }

                    return idMeta;
                } else {
                    throw new SQLException("Inserimento meta-informazione fallito, nessun ID ottenuto.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento della meta-informazione: " + e.getMessage(), e);
        }
    }

/*Caused by: java.lang.NullPointerException: Cannot invoke "String.split(String)" because "autoriStr" is null
	at com.dashapp.model.CatalogoDao.getBrani(CatalogoDao.java:39)
	at com.dashapp.controller.CatalogoController.initialize(CatalogoController.java:42)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
	... 64 more
Error loading view: catalogo.fxml*/

    // Inserisce strumenti o esecutori per una nota/meta
    public boolean inserisciStrumenti(boolean isStrumento, boolean isMeta, int entityId, List<Integer> lista) {
        String table;
        String column;

        if (isMeta) {
            table = isStrumento ? "meta_strumenti" : "meta_esecutori";
        } else {
            table = isStrumento ? "note_strumenti" : "note_esecutori";
        }

        column = isStrumento ? "strumento" : "esecutore";
        String foreignKey = isMeta ? "meta" : "nota";

        String query = "INSERT INTO " + table + " (" + foreignKey + ", " + column + ") VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (Integer id : lista) {
                stmt.setInt(1, entityId);
                stmt.setInt(2, id);
                stmt.addBatch();
            }

            stmt.executeBatch();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento in " + table + ": " + e.getMessage(), e);
        }
    }

    // Recupera strumenti o esecutori
    public Map<Integer, String> getOthers(boolean isStrumento) {
        Map<Integer, String> others = new HashMap<>();
        String tableName = isStrumento ? "strumenti" : "esecutori";
        String query = "SELECT id, nome FROM " + tableName;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                others.put(rs.getInt("id"), rs.getString("nome"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero da " + tableName + ": " + e.getMessage(), e);
        }

        return others;
    }

    // Aggiunge uno strumento o esecutore
    public boolean addOthers(boolean isStrumento, String nome) {
        String tableName = isStrumento ? "strumenti" : "esecutori";
        String query = "INSERT INTO " + tableName + " (nome) VALUES (?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nome);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento in " + tableName + ": " + e.getMessage(), e);
        }
    }

    // Recupera le meta-informazioni per un brano
    public List<MetaBean> getMetaPerBrano(int idBrano) {
        List<MetaBean> metaList = new ArrayList<>();
        String queryMeta = """
            SELECT id, brano, utente, titolo_brano, data_inserimento, inizio, fine, commentoBean
            FROM meta
            WHERE brano = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmtMeta = conn.prepareStatement(queryMeta)) {

            stmtMeta.setInt(1, idBrano);
            try (ResultSet rsMeta = stmtMeta.executeQuery()) {
                while (rsMeta.next()) {
                    MetaBean meta = new MetaBean(
                            rsMeta.getInt("id"),
                            rsMeta.getInt("brano"),
                            rsMeta.getInt("utente"),
                            rsMeta.getString("titolo_brano"),
                            rsMeta.getDate("data_inserimento"),
                            rsMeta.getTime("inizio"),
                            rsMeta.getTime("fine"),
                            rsMeta.getString("commentoBean")
                    );
                    System.out.println(meta.toString());
                    meta.setEsecutori(getEsecutoriPerMeta(meta.getId(), conn));
                    meta.setStrumenti(getStrumentiPerMeta(meta.getId(), conn));

                    metaList.add(meta);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle meta-informazioni: " + e.getMessage(), e);
        }

        return metaList;
    }

    private List<String> getEsecutoriPerMeta(int metaId, Connection conn) throws SQLException {
        String query = """
            SELECT e.nome
            FROM esecutori e
            JOIN meta_esecutori me ON e.id = me.esecutore
            WHERE me.meta = ?
            """;
        List<String> esecutori = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, metaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    esecutori.add(rs.getString("nome"));
                }
            }
        }
        return esecutori;
    }

    private List<String> getStrumentiPerMeta(int metaId, Connection conn) throws SQLException {
        String query = """
            SELECT s.nome
            FROM strumenti s
            JOIN meta_strumenti ms ON s.id = ms.strumento
            WHERE ms.meta = ?
            """;
        List<String> strumenti = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, metaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    strumenti.add(rs.getString("nome"));
                }
            }
        }
        return strumenti;
    }
}
