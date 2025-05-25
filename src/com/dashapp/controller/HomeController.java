package com.dashapp.controller;

import com.dashapp.view.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Button first;

    @FXML
    private Button second;

    @FXML
    private Button third;

    @FXML
    private Text titolo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        titolo.setText("Benvenuto nella home "+ViewNavigator.getAuthenticatedUser());
    }

    @FXML
    private void handleFirstButton(ActionEvent event) {
        ViewNavigator.changeTitle("Caricamento brani");
        ViewNavigator.navigateToCarica();
    }

    @FXML
    private void handleSecondButton(ActionEvent event) {
        ViewNavigator.changeTitle("Catalogo brani");
        ViewNavigator.navigateToCatalogo();
    }

    @FXML
    private void handleThirdButton(ActionEvent event) {
        ViewNavigator.navigateToHome();
    }
}