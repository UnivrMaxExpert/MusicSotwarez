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

public class CaricaDao
{
    private String path;
    public CaricaDao(){}

    public boolean caricaBrano(BranoBean brano)
    {
        String sql = "INSERT INTO brani (titolo, genere, autori, file, anno) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, brano.getTitolo());
            stmt.setString(2, brano.getGenere().toString());
            stmt.setString(3, brano.getAutori());
            stmt.setString(4, brano.getFile());
            stmt.setInt(5, brano.getAnno());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String openfilechooser(Stage stage)
    {
        FileChooser fileChooser = new FileChooser();

        // Aggiungi i filtri per i file (mp3, mp4, pdf, jpg, jpeg)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Audio", "*.mp3"),
                new FileChooser.ExtensionFilter("File Video", "*.mp4"),
                new FileChooser.ExtensionFilter("Documenti PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Immagini JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("Immagini JPEG", "*.jpeg")
        );

        // Mostra la finestra di dialogo per la selezione del file
        File selectedFile = fileChooser.showSaveDialog(stage);

        // Verifica se un file è stato selezionato
        //La parte di scelta del file deve essere gestita da un model richiamato in questa classe DA MODIFICARE
        if (selectedFile != null) {
            try {
                // Ottieni il percorso della cartella user_files/file1
                Path baseDir = Path.of("src/com/dashapp/user_files/"+ ViewNavigator.getAuthenticatedUser());

                // Crea le cartelle se non esistono
                if (!Files.exists(baseDir)) {
                    Files.createDirectories(baseDir);
                }

                // Costruisci il percorso di destinazione
                Path destination = baseDir.resolve(selectedFile.getName());

                // Copia il file
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("File copiato in: " + destination.toAbsolutePath());
                return destination.toAbsolutePath().toString();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Errore nella copia del file.");
            }
        } else {
            System.out.println("Nessun file selezionato.");
            return null;
        }
        return null;
    }


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
                new FileChooser.ExtensionFilter("File Video", "*.mp4")
        );

        // ✅ Salva, NON apri
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
