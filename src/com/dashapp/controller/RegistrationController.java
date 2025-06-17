package com.dashapp.controller;

import com.dashapp.view.AnimationUtil;
import com.dashapp.view.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.dashapp.model.UtenteBean;
import com.dashapp.model.AccessoDao;
    /*Controller che gestisce la registrazione degli utenti*/
public class RegistrationController implements Initializable
{
    @FXML private TextField username;
    @FXML private VBox registerBox;
    @FXML private Pane blurLayer;
    @FXML private PasswordField password;
    @FXML private Button reg;
    @FXML private Label errorLabel;

    @FXML
    private void handleRegistration(ActionEvent event) throws IOException, SQLException {
        String usernames = username.getText();
        String passwords = password.getText();

        UtenteBean utente = new UtenteBean(usernames, passwords);
        AccessoDao acc = new AccessoDao();
        if(acc.registrazioneControllo(utente))
        {
            ViewNavigator.changeTitle("Login applicazione");
            ViewNavigator.navigateToLogin();
        }
        else
            showError();
    }

    public void handleGoToLogin(ActionEvent actionEvent) {
        ViewNavigator.navigateToLogin();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        blurLayer.setEffect(new GaussianBlur(25));
        BoxBlur blur = new BoxBlur(10, 10, 3);
        registerBox.setEffect(blur);

        AnimationUtil.fadeIn(registerBox, 600);
        AnimationUtil.slideInFromTop(registerBox, 600, 300);
    }
    private void showError() {
        errorLabel.setText("Credenziali non corrette!");
        errorLabel.setVisible(true);
        AnimationUtil.fadeIn(errorLabel, 10);
    }
}
