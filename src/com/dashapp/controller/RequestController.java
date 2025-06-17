package com.dashapp.controller;

import com.dashapp.model.RequestDao;
import com.dashapp.model.UtenteBean; // Assicurati che UtenteBean sia in questo package
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;

import java.util.List;
import java.util.Optional;
    /*Gestisce la stampa delle varie richieste per essere registrati al software, dove l'admin sceglie se accettarle oppure no*/
public class RequestController {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox box; // Questo VBox è il container delle richieste, dovrebbe avere fx:id="box" nell'FXML

    private RequestDao reqDAO = new RequestDao(); // Rinomino per chiarezza, meglio 'reqDAO' o 'requestDAO'

    // Metodo chiamato automaticamente dopo che i campi @FXML sono stati iniettati
    @FXML
    public void initialize() {
        loadRichieste(); // Carica le richieste all'avvio del controller
    }

    private void loadRichieste() {
        box.getChildren().clear(); // Pulisci il VBox prima di ricaricare le richieste

        List<UtenteBean> richieste = null;
        try {
            richieste = reqDAO.getRichieste();
        } catch (RuntimeException e) {
            // Gestisci l'errore di caricamento (es. mostra un messaggio all'utente)
            System.err.println("Errore durante il caricamento delle richieste: " + e.getMessage());
            Text errorText = new Text("Errore durante il caricamento delle richieste dal database.");
            errorText.setStyle("-fx-fill: red;");
            box.getChildren().add(errorText);
            return;
        }


        if (richieste != null && !richieste.isEmpty()) {
            for (UtenteBean utente : richieste) {
                // Crea una nuova HBox per ogni richiesta
                HBox requestHBox = new HBox();
                requestHBox.setSpacing(20.0);
                requestHBox.setAlignment(Pos.CENTER_LEFT);

                // Aggiungi il testo della richiesta (es. Username)
                Text requestText = new Text(utente.getUsername()); // Usiamo l'username come identificativo visibile
                requestHBox.getChildren().add(requestText);

                // Aggiungi il bottone Approva
                Button approvaButton = new Button("Approva");
                // Imposta l'azione del bottone. Usiamo una lambda per passare l'UtenteBean specifico.
                // Questo è cruciale per sapere quale utente approvare.
                approvaButton.setOnAction(e -> handleApprova(e, utente, requestHBox));
                requestHBox.getChildren().add(approvaButton);

                // Aggiungi il bottone Rifiuta
                Button rifiutaButton = new Button("Rifiuta");
                // Imposta l'azione del bottone, passando l'UtenteBean specifico.
                rifiutaButton.setOnAction(e -> handleRifiuta(e, utente, requestHBox));
                requestHBox.getChildren().add(rifiutaButton);

                // Aggiungi l'HBox al VBox principale
                box.getChildren().add(requestHBox);
            }
        } else {
            Text noRequestsText = new Text("Nessuna richiesta in sospeso.");
            box.getChildren().add(noRequestsText);
        }
    }

    // Metodo per gestire l'azione del bottone "Approva"
    // Ora prende anche l'UtenteBean e l'HBox associata
    public void handleApprova(ActionEvent event, UtenteBean utenteToApprove, HBox requestHBox) {
        System.out.println("Richiesta di approvazione per utente: " + utenteToApprove.getUsername());

        boolean success = reqDAO.approvaRichiesta(utenteToApprove);

        if (success) {
            System.out.println("Richiesta di " + utenteToApprove.getUsername() + " approvata con successo!");
            // Rimuovi l'HBox dalla UI solo se l'operazione sul DB ha avuto successo
            box.getChildren().remove(requestHBox);
        } else {
            System.err.println("Fallimento nell'approvazione della richiesta per " + utenteToApprove.getUsername());
            // Potresti mostrare un alert all'utente qui
        }
    }

    // Metodo per gestire l'azione del bottone "Rifiuta"
    // Ora prende anche l'UtenteBean e l'HBox associata
    public void handleRifiuta(ActionEvent event, UtenteBean utenteToReject, HBox requestHBox) {
        System.out.println("Richiesta di rifiuto per utente: " + utenteToReject.getUsername());

        String motivo = mostraDialogMotivo();

        if (motivo != null && !motivo.trim().isEmpty()) {
            boolean esito = reqDAO.rifiutaRichiesta(utenteToReject, motivo);
            if (esito) {
                showAlert(Alert.AlertType.INFORMATION, "Richiesta rifiutata con successo.");// aggiorna la tabella o lista
            } else {
                showAlert(Alert.AlertType.ERROR, "Errore nel rifiuto della richiesta.");
            }
        }

        boolean success = reqDAO.rifiutaRichiesta(utenteToReject, null);

        if (success) {
            System.out.println("Richiesta di " + utenteToReject.getUsername() + " rifiutata con successo!");
            // Rimuovi l'HBox dalla UI solo se l'operazione sul DB ha avuto successo
            box.getChildren().remove(requestHBox);
        } else {
            System.err.println("Fallimento nel rifiuto della richiesta per " + utenteToReject.getUsername());
            // Potresti mostrare un alert all'utente qui
        }
    }

    private String mostraDialogMotivo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Motivo del Rifiuto");
        dialog.setHeaderText("Inserisci il motivo del rifiuto della richiesta");
        dialog.setContentText("Motivo:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}