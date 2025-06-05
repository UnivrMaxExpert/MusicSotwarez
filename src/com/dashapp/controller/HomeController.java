package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.Genere;
import com.dashapp.view.ViewNavigator;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
        first.setFocusTraversable(false);
        second.setFocusTraversable(false);
        third.setFocusTraversable(false);
        caricaBranoCommentati();
    }

    private void caricaBranoCommentati() {
/*
        HBox hbox = new HBox(15);
        hbox.getStyleClass().add("brano-card");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPrefHeight(110);
        hbox.setPrefWidth(650);
        hbox.setPadding(new Insets(15));

        VBox vbox = new VBox(6);
        vbox.setAlignment(Pos.CENTER_LEFT);

        Text titoloText = new Text(titolo);
        titoloText.getStyleClass().add("brano-title");

        Text autoreText = new Text("Autore: " + autore);
        autoreText.getStyleClass().add("brano-meta");

        Text dettagliText = new Text(genere + " • " + anno);
        dettagliText.getStyleClass().add("brano-meta");

        vbox.getChildren().addAll(titoloText, autoreText, dettagliText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

// Pulsante play JFoenix
        JFXButton playButton = new JFXButton("▶");
        playButton.getStyleClass().add("circular-button");

        playButton.setOnAction(e -> {
            System.out.println("Riproduzione: " + path);
            Genere genereEnum = Genere.valueOf(genere.toUpperCase());
            String[] autoriArray = autore.split("\\s*,\\s*");
            BranoBean brano = new BranoBean(titolo, genereEnum, path, anno, autoriArray);
            brano.setId(id);
            ViewNavigator.setBrano(brano);
            ViewNavigator.navigateToBrano();
        });

        hbox.getChildren().addAll(vbox, spacer, playButton);
        braniContainer.getChildren().add(hbox);*/
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
        ViewNavigator.navigateToExit();
    }
}