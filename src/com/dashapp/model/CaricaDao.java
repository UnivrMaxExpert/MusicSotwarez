package com.dashapp.model;

import com.dashapp.util.DatabaseManager;
import com.dashapp.util.PasswordHasher;
import com.dashapp.view.ViewNavigator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CaricaDao {
    private Path destination;

    public CaricaDao() {
    }

    public boolean caricaBrano(BranoBean brano) {
        String sql = "INSERT INTO brani (titolo, genere, autori, file, anno, concerto) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, brano.getTitolo());
            stmt.setString(2, brano.getGenere().toString());
            stmt.setString(3, brano.getAutori());
            stmt.setString(4, brano.getFile());
            stmt.setInt(5, brano.getAnno());
            stmt.setBoolean(6, brano.isConcerto());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    /**
     * Permette di scaricare un brano (copia su destinazione selezionata dall'utente).
     */
    public String scaricaBrano(Stage stage, String percorsoSorgente) {
        if (percorsoSorgente == null || percorsoSorgente.isBlank()) {
            System.out.println("Percorso del brano non valido.");
            return null;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva brano come...");
        fileChooser.setInitialFileName(Paths.get(percorsoSorgente).getFileName().toString());

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Audio", "*.mp3"),
                new FileChooser.ExtensionFilter("File Video", "*.mp4"),
                new FileChooser.ExtensionFilter("Documenti PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Immagini JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("Immagini JPEG", "*.jpeg")
        );

        File destinazione = fileChooser.showSaveDialog(stage);
        if (destinazione != null) {
            try {
                Path sorgente = Paths.get(percorsoSorgente);
                Files.copy(sorgente, destinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Brano salvato in: " + destinazione.getAbsolutePath());
                return destinazione.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Errore durante la copia del file.");
            }
        } else {
            System.out.println("Salvataggio annullato dall'utente.");
        }
        return null;
    }
}
