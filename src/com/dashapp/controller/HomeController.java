package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.CaricaDao;
import com.dashapp.model.CatalogoDao;
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
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    /*Home per gli utenti dove scelgono se caricare brani, visualizzare il catalogo oppure uscire <-ultima non implementata ma semplice
    * C'è anche un pannello dove si vedono i vari brani su cui si è lasciato un commento*/
    @FXML
    private Button first;

    @FXML
    private Button second;
    @FXML private VBox vboxBrani;
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
        CatalogoDao cat  = new CatalogoDao();
        List<BranoBean> lista = cat.getBraniCommentati();
        System.out.println(lista.size());
        for (BranoBean b : lista)
            caricaBranoCommentati(b);
    }

    private void caricaBranoCommentati(BranoBean b)
    {//NB: Meglio fare un HomeView per la parte grafica da richiamare qua come oggetto
        HBox hbox = new HBox(10);
        hbox.getStyleClass().add("brano-card");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(10));
        hbox.setMaxWidth(Double.MAX_VALUE);  // Così si adatta al VBox contenitore

        VBox vbox = new VBox(4);
        vbox.setAlignment(Pos.CENTER_LEFT);

        Text titoloText = new Text(b.getTitolo());
        titoloText.getStyleClass().add("brano-title");

        Text autoreText = new Text("Autore: " + b.getAutori());
        autoreText.getStyleClass().add("brano-meta");

        Text dettagliText = new Text(b.getGenere() + " • " + b.getAnno());
        dettagliText.getStyleClass().add("brano-meta");

        /*Text postatore = new Text("Postato da: "+b);
        postatore.getStyleClass().add("brano-meta");*/

        vbox.getChildren().addAll(titoloText, autoreText, dettagliText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

// Pulsante play JFoenix
        JFXButton playButton = new JFXButton("▶");
        playButton.getStyleClass().add("circular-button");

        playButton.setOnAction(e -> {
            System.out.println("Riproduzione: " + b.getFile());
            ViewNavigator.setBrano(b);
            ViewNavigator.navigateToBrano();
        });

        hbox.getChildren().addAll(vbox, spacer, playButton);
        vboxBrani.getChildren().add(hbox);
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