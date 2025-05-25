package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.CatalogoDao;
import com.dashapp.model.Genere;
import com.dashapp.view.AnimationUtil;
import com.dashapp.view.ViewNavigator;
import com.jfoenix.controls.JFXButton;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

public class CatalogoController {

    @FXML
    private VBox sidebar;

    @FXML
    private Button filterButton;

    @FXML
    private BorderPane root;

    @FXML
    private VBox braniContainer;

    private boolean sidebarVisible = false;

    @FXML
    public void initialize() {
        // Sidebar non visibile
        root.setLeft(null);

        CatalogoDao cat  = new CatalogoDao();
        List<BranoBean> lists = cat.getBrani();
        lists.forEach(branoBean -> aggiungiBrano(branoBean.getId(), branoBean.getTitolo(), branoBean.getAutori(), branoBean.getGenere().toString(), branoBean.getAnno(), branoBean.getFile()));
    }

    @FXML
    private void toggleSidebar() {
        if (sidebarVisible) {
            // Anima e poi rimuovi
            TranslateTransition transition = new TranslateTransition(Duration.millis(200), sidebar);
            transition.setFromX(0);
            transition.setToX(-sidebar.getWidth());
            transition.setOnFinished(e -> {
                root.setLeft(null); // Rimuove la sidebar completamente dal BorderPane
            });
            transition.play();
        } else {
            // Riaggiungi la sidebar e anima
            root.setLeft(sidebar); // Aggiunge la sidebar di nuovo nel BorderPane

            sidebar.setTranslateX(-sidebar.getWidth()); // Preparazione per animazione
            TranslateTransition transition = new TranslateTransition(Duration.millis(200), sidebar);
            transition.setFromX(-sidebar.getWidth());
            transition.setToX(0);
            transition.play();
        }

        sidebarVisible = !sidebarVisible;
    }

    public void aggiungiBrano(int id, String titolo, String autore, String genere, int anno, String path) {
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
        braniContainer.getChildren().add(hbox);
    }

}
