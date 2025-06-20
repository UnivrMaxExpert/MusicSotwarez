package com.dashapp.model;

import com.dashapp.util.DatabaseManager;
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

    public int scaricaBrano(Stage stage, String percorsoSorgente) {
        if (percorsoSorgente == null || percorsoSorgente.isBlank()) {
            System.out.println("Percorso del brano non valido.");
            return -1;
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
                return 1;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Errore durante la copia del file.");
            }
        } else {
            return 0;
        }
        return -1;
    }
}
