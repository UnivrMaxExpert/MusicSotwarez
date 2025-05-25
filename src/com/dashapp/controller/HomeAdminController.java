package com.dashapp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class HomeAdminController {

    @FXML
    private Button first;

    @FXML
    private Button second;

    @FXML
    private Button third;

    @FXML
    private void handleFirstButton(ActionEvent event) {
        loadScene("reg.fxml", event);
    }

    @FXML
    private void handleSecondButton(ActionEvent event) {
        loadScene("catalogo.fxml", event);
    }

    @FXML
    private void handleThirdButton(ActionEvent event) {
        loadScene("esci.fxml", event);
    }

    private void loadScene(String fxmlFile, ActionEvent event) {
        try {
            URL resource = getClass().getResource("/resources/fxml/" + fxmlFile); // Assicurati che i file FXML siano nella cartella delle risorse
            if (resource == null) {
                System.err.println("File FXML non trovato: " + fxmlFile);
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Scene scene = ((Button) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene(); // Opzionale: ridimensiona la finestra in base al nuovo contenuto
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento del file FXML: " + fxmlFile);
        }
    }
}