package com.dashapp.controller; // Assicurati che il package sia corretto

import com.dashapp.util.DatabaseManager;
import com.dashapp.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
    /*Classe di prova che non c'entra nulla solo per testare alcune cose*/
public class ProvaController implements Initializable {
    @FXML private MediaView mediaView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String uri = Paths.get(ViewNavigator.getBrano().getFile()).toUri().toString();
        Media media = new Media(uri);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        ViewNavigator.setRisorsa(mediaPlayer);
        mediaView.setMediaPlayer(mediaPlayer);
    }
}