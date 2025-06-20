package com.dashapp.model;

import com.dashapp.util.DatabaseManager;
import com.dashapp.util.PasswordHasher;
import com.dashapp.view.ViewNavigator;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
    /*Permette di caricare eventuali informazioni di un brano nel db, mentre il file effettivo è salvato in locale*/
public class CaricaDao {
    private Path destination;

    public CaricaDao() {
    }

    public int caricaBrano(BranoBean brano) {
        String sql = "INSERT INTO brani (utente, titolo, genere, file, anno, concerto) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, brano.getUtente());
            stmt.setString(2, brano.getTitolo());
            stmt.setString(3, brano.getGenere().toString());
            stmt.setString(4, brano.getFile());
            stmt.setObject(5, brano.getAnno(), java.sql.Types.INTEGER);
            stmt.setBoolean(6, brano.isConcerto());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento brano fallito.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerato = generatedKeys.getInt(1);
                    brano.setId(idGenerato);
                    if(!caricaAutoriBrano(Arrays.stream(brano.getAutori().split(",")).toList()))
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Errore nel caricamento del brano");
                        alert.showAndWait();
                        return -1;
                    }
                    return idGenerato;
                } else {
                    throw new SQLException("ID del brano non restituito.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean caricaAutoriBrano(List<String> esecutori) {
        String checkQuery = "SELECT ruolo FROM esecutori WHERE nome = ?";
        String insertQuery = "INSERT INTO esecutori (nome, ruolo) VALUES (?, 'autore')";
        String updateQuery = "UPDATE esecutori SET ruolo = 'entrambi' WHERE nome = ?";

        try (Connection conn = DatabaseManager.getConnection();) {
            conn.setAutoCommit(false);  // Per sicurezza, gestione transazioni

            try (
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery)
            ) {
                for (String nome : esecutori) {
                    checkStmt.setString(1, nome);
                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next()) {
                        // Non esiste: lo inseriamo come autore
                        insertStmt.setString(1, nome);
                        insertStmt.executeUpdate();
                    } else {
                        String ruoloEsistente = rs.getString("ruolo");
                        if ("esecutore".equalsIgnoreCase(ruoloEsistente)) {
                            // È già presente come esecutore: aggiorniamo a "entrambi"
                            updateStmt.setString(1, nome);
                            updateStmt.executeUpdate();
                        }
                        // Se è già autore o entrambi, non facciamo nulla
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();  // Rollback in caso di errore
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean caricaStrumentiPerBrano(int idBrano, List<String> strumenti) {
        String selectStrumentoSQL = "SELECT id FROM strumenti WHERE nome = ?";
        String insertStrumentoSQL = "INSERT INTO strumenti (nome) VALUES (?)";
        String insertJoinSQL = "INSERT INTO brani_strumenti (brano, strumento) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectStrumentoSQL);
             PreparedStatement insertStrumentoStmt = conn.prepareStatement(insertStrumentoSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertJoinStmt = conn.prepareStatement(insertJoinSQL)) {

            for (String nomeStrumento : strumenti) {
                int idStrumento = -1;

                // 1. Controllo se lo strumento esiste già
                selectStmt.setString(1, nomeStrumento);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        idStrumento = rs.getInt("id");
                    }
                }

                // 2. Se non esiste, lo inserisco
                if (idStrumento == -1) {
                    insertStrumentoStmt.setString(1, nomeStrumento);
                    insertStrumentoStmt.executeUpdate();
                    try (ResultSet generatedKeys = insertStrumentoStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            idStrumento = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Inserimento strumento fallito: ID non generato.");
                        }
                    }
                    System.out.println("Strumento inserito: " + nomeStrumento + " con ID " + idStrumento);
                }

                // 3. Inserisco nella tabella di join
                insertJoinStmt.setInt(1, idBrano);
                insertJoinStmt.setInt(2, idStrumento);
                insertJoinStmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean caricaArtistiPerBrano(int idBrano, List<String> artisti)
    {
        String selectStrumentoSQL = "SELECT id FROM esecutori WHERE nome = ?";
        String insertStrumentoSQL = "INSERT INTO esecutori (nome) VALUES (?)";
        String insertJoinSQL = "INSERT INTO brani_artisti (brano, artista) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectStrumentoSQL);
             PreparedStatement insertStrumentoStmt = conn.prepareStatement(insertStrumentoSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertJoinStmt = conn.prepareStatement(insertJoinSQL)) {

            for (String nomeArtista : artisti) {
                int idStrumento = -1;

                // 1. Controllo se lo strumento esiste già
                selectStmt.setString(1, nomeArtista);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        idStrumento = rs.getInt("id");
                    }
                }

                // 2. Se non esiste, lo inserisco
                if (idStrumento == -1) {
                    insertStrumentoStmt.setString(1, nomeArtista);
                    insertStrumentoStmt.executeUpdate();
                    try (ResultSet generatedKeys = insertStrumentoStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            idStrumento = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Inserimento artista fallito: ID non generato.");
                        }
                    }
                    System.out.println("Artista inserito: " + nomeArtista + " con ID " + idStrumento);
                }

                // 3. Inserisco nella tabella di join
                insertJoinStmt.setInt(1, idBrano);
                insertJoinStmt.setInt(2, idStrumento);
                insertJoinStmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public Ruoli autoInterpretato(BranoBean brano, String nuovoRuolo) {
        String selectSQL = "SELECT ruolo FROM brani_utenti WHERE brano = ? AND utente = ?";
        String updateSQL = "UPDATE brani_utenti SET ruolo = ? WHERE brano = ? AND utente = ?";
        String insertSQL = "INSERT INTO brani_utenti (brano, utente, ruolo) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
             PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

            selectStmt.setInt(1, brano.getId());
            selectStmt.setInt(2, brano.getUtente());
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // Esiste già, quindi aggiorno
                updateStmt.setString(1, nuovoRuolo);
                updateStmt.setInt(2, brano.getId());
                updateStmt.setInt(3, brano.getUtente());
                updateStmt.executeUpdate();
                System.out.println("Record aggiornato con ruolo: " + nuovoRuolo);
            } else {
                // Non esiste, quindi inserisco
                insertStmt.setInt(1, brano.getId());
                insertStmt.setInt(2, brano.getUtente());
                insertStmt.setString(3, nuovoRuolo);
                insertStmt.executeUpdate();
                System.out.println("Record inserito con ruolo: " + nuovoRuolo);
            }

            // Restituisco il nuovo ruolo come enum
            return Ruoli.valueOf(nuovoRuolo.toUpperCase());

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Valore ruolo non valido: " + nuovoRuolo);
            return null;
        }
    }

    /**
     * Permette di selezionare un file da importare tramite FileChooser.
     * Ritorna il path assoluto del file selezionato oppure null.
     */
    public String openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona un file");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Audio", "*.mp3"),
                new FileChooser.ExtensionFilter("File Video", "*.mp4"),
                new FileChooser.ExtensionFilter("Documenti PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Immagini JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("Immagini JPEG", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            System.out.println("Nessun file selezionato.");
            return null;
        }
    }

    /**
     * Copia un file selezionato nella cartella dell'utente autenticato.
     * Ritorna il path assoluto della destinazione.
     */
    public String copiaBrano(String selectedFilePath) throws IOException {
        if (selectedFilePath == null || selectedFilePath.isBlank()) {
            throw new IllegalArgumentException("Il percorso del file sorgente non è valido.");
        }

        Path source = Paths.get(selectedFilePath);
        if (!Files.exists(source)) {
            throw new IOException("Il file sorgente non esiste.");
        }

        Path baseDir = Paths.get("src", "com", "dashapp", "user_files", ViewNavigator.getAuthenticatedUser());
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        Path destination = baseDir.resolve(source.getFileName());
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("File copiato in: " + destination.toAbsolutePath());
        return destination.toAbsolutePath().toString();
    }

}
