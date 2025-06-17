package com.dashapp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.dashapp.view.ViewNavigator;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;
    /*Controller che gestisce la navbar che viene importata in ogni fxml*/
public class NavbarController implements Initializable {
    @FXML
    private Button home, carica, catalogo;

    @FXML
    private HBox nav;

    @FXML
    private void handleHome(ActionEvent event) {
        ViewNavigator.navigateToHome();
    }

    @FXML
    private void handleCarica(ActionEvent event) {
        ViewNavigator.navigateToCarica();
    }

    @FXML
    private void handleCatalogo(ActionEvent event) {
        ViewNavigator.navigateToCatalogo();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        switch (ViewNavigator.getState()) {
            case 3:
                carica.setVisible(false);
                carica.setManaged(false);
                break;
            case 4:
                catalogo.setVisible(false);
                catalogo.setManaged(false);
                break;
            case 5:
                carica.setVisible(true);
                carica.setManaged(true);
                catalogo.setVisible(true);
                catalogo.setManaged(true);
                break;
        }
    }

}