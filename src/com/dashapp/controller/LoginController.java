package com.dashapp.controller;

import com.dashapp.model.AccessoDao;
import com.dashapp.model.RequestDao;
import com.dashapp.model.UtenteBean;
import com.dashapp.view.AnimationUtil;
import com.dashapp.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    /*Controller per la pagina di Login che gestisce gli utenti registrati nella tabella utenti del db, ma anche quelli che hanno fatto la richiesta
    * nella tabella richieste, decidendo in base allo loro stato (in attesa, approvato, rifiutato, standard <- utente normale), se accettarli per il
    * login o rifiutarli, o se sono approvati, aprirgli la finestra dove gli si dice che sono stati accettati dall'admin*/
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox loginBox;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Pane blurLayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        blurLayer.setEffect(new GaussianBlur(25));
        BoxBlur blur = new BoxBlur(10, 10, 3);
        loginBox.setEffect(blur);

        AnimationUtil.fadeIn(loginBox, 600);
        AnimationUtil.slideInFromBottom(loginBox, 600, 300);
    }

    @FXML
    private void handleLogin(ActionEvent event) throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showError("Compila tutti i campi.");
            AnimationUtil.shake(loginBox, 10);
            return;
        }

        UtenteBean utente = new UtenteBean(username, password);
        AccessoDao acc = new AccessoDao();
        RequestDao requestDao = new RequestDao();

        // 1. Login nella tabella utenti
        if (acc.loginControllo(utente)) {
            ViewNavigator.setAuthenticatedUser(username);

            if (utente.isAdmin()) {
                ViewNavigator.changeTitle("Richieste");
                ViewNavigator.navigateToRequest();
            } else {
                ViewNavigator.changeTitle("Home applicazione");
                ViewNavigator.navigateToHome();
            }
            return;
        }

        // 2. Controlla stato in richieste
        String statoRichiesta = requestDao.getStatoRichiesta(username);

        switch (statoRichiesta) {
            case "IN_ATTESA":
                mostraModalInfo("Richiesta in attesa", "La tua richiesta è ancora in attesa di approvazione.");
                break;

            case "RIFIUTATO":
                mostraModalInfo("Richiesta rifiutata", "La tua richiesta è stata rifiutata. Contatta l'amministratore.");
                break;

            case "APPROVATO":
                mostraModalBenvenuto(username);
                if (requestDao.trasferisciUtenteDaRichiestaAUtenti(username, password)) {
                    requestDao.aggiornaStatoRichiesta(username, "STANDARD");
                    ViewNavigator.setAuthenticatedUser(username);
                    ViewNavigator.changeTitle("Home applicazione");
                    ViewNavigator.navigateToHome();
                } else {
                    showError("Errore nel trasferimento dell'account. Contatta il supporto.");
                }
                break;

            default:
                showError("Credenziali errate o utente non registrato.");
                break;
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) throws IOException {
        ViewNavigator.navigateToRegister();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        AnimationUtil.fadeIn(errorLabel, 10);
    }

    private void mostraModalBenvenuto(String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Benvenuto");
        alert.setHeaderText("Account approvato");
        alert.setContentText("Benvenuto " + username + ", il tuo account è stato approvato dall'amministratore!");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    private void mostraModalInfo(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
}
