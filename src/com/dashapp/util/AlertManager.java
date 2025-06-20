package com.dashapp.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertManager {

    public static void showError(String message) {
        showAlert(AlertType.ERROR, "Errore", message);
    }

    public static void showInfo(String message) {
        showAlert(AlertType.INFORMATION, "Informazione", message);
    }

    public static void showWarning(String message) {
        showAlert(AlertType.WARNING, "Attenzione", message);
    }

    public static void showConfirmation(String message) {
        showAlert(AlertType.CONFIRMATION, "Conferma", message);
    }

    private static void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Nessun header, solo il messaggio
        alert.setContentText(message);
        alert.showAndWait(); // Aspetta la chiusura dell’alert
    }
}

