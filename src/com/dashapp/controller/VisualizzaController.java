package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.CaricaDao;
import com.dashapp.model.Commento;
import com.dashapp.model.CommentoDao;
import com.dashapp.view.ViewNavigator;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizzaController {

    @FXML private MediaView mediaView;
    @FXML private Button toggleButton;
    @FXML private Button forwardButton;
    @FXML private Button rewindButton;
    @FXML private Slider progressSlider;
    @FXML private Label timeLabel;
    @FXML private VBox commentSection;
    @FXML private Slider volumeSlider;
    @FXML private StackPane volumeContainer;
    @FXML private TextField newCommentField;
    @FXML private Button pubblicaButton;
    @FXML private Button scaricaButton;
    @FXML private ImageView audioPlaceholder;
    private MediaPlayer mediaPlayer;
    private BranoBean brano = ViewNavigator.getBrano();;
    private final CommentoDao commentoDao = new CommentoDao();
    private Integer commentoSelezionatoPerRisposta = null;
    private CaricaDao carica;
    @FXML
    private void scaricaBrano() {
        if (brano == null || brano.getFile() == null) {
            System.out.println("Nessun brano selezionato.");
            return;
        }
        carica = new CaricaDao();
        String risultato = carica.scaricaBrano(
                (Stage) scaricaButton.getScene().getWindow(),
                brano.getFile()
        );

        if (risultato != null) {
            // opzionalmente mostra notifica o alert
            System.out.println("Download completato.");
        }
    }

    @FXML
    private void showVolumeSlider() {
        volumeSlider.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), volumeSlider);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @FXML
    private void hideVolumeSlider() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), volumeSlider);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> volumeSlider.setVisible(false));
        fadeOut.play();
    }

    public void initialize() {
        String videoPath = new File(brano.getFile()).toURI().toString();
        if (brano.getFile().endsWith(".mp3") || brano.getFile().endsWith(".wav")) {
            mediaView.setVisible(false);
            mediaView.setManaged(false);
            audioPlaceholder.setVisible(true);
            audioPlaceholder.setManaged(true);
            audioPlaceholder.setImage(new Image(getClass().getResourceAsStream("/resources/images/placeholder.gif")));
        } else {
            mediaView.setVisible(true);
            mediaView.setManaged(true);
            audioPlaceholder.setVisible(false);
            audioPlaceholder.setManaged(false);
        }
        Media media = new Media(videoPath);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        toggleButton.setOnAction(e -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                toggleButton.setText("▶");
            } else {
                mediaPlayer.play();
                toggleButton.setText("⏸");
            }
        });

        rewindButton.setOnAction(e -> {
            Duration current = mediaPlayer.getCurrentTime();
            Duration target = current.subtract(Duration.seconds(5));
            mediaPlayer.seek(target.lessThan(Duration.ZERO) ? Duration.ZERO : target);
        });

        forwardButton.setOnAction(e -> {
            Duration current = mediaPlayer.getCurrentTime();
            Duration total = mediaPlayer.getTotalDuration();
            Duration target = current.add(Duration.seconds(5));
            mediaPlayer.seek(target.greaterThan(total) ? total : target);
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newTime.toSeconds());
            }

            Duration total = mediaPlayer.getTotalDuration();
            String currentStr = formatTime(newTime);
            String totalStr = formatTime(total);
            timeLabel.setText(currentStr + " / " + totalStr);
        });

        mediaPlayer.setOnReady(() -> {
            progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            mediaPlayer.setVolume(volumeSlider.getValue());
        });

        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            }
        });

        progressSlider.setOnMouseReleased(e -> {
            mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            mediaPlayer.setVolume(newVal.doubleValue());
        });

        pubblicaButton.setOnAction(e -> {
            String testo = newCommentField.getText().trim();
            if (!testo.isEmpty()) {
                try {
                    int utenteId = ViewNavigator.getUtenteId(); // deve essere implementato
                    commentoDao.aggiungiCommento(brano.getId(), utenteId, testo, commentoSelezionatoPerRisposta);
                    newCommentField.clear();
                    commentoSelezionatoPerRisposta = null;
                    newCommentField.setPromptText("Aggiungi un commento...");
                    caricaCommenti();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        caricaCommenti();
    }

    private void caricaCommenti() {
        commentSection.getChildren().clear();
        try {
            List<Commento> commenti = commentoDao.getCommentiByBranoId(brano.getId());
            Map<Integer, VBox> mappaCommenti = new HashMap<>();

            for (Commento c : commenti) {
                VBox commentBox = creaBoxCommento(c);
                mappaCommenti.put(c.getId(), commentBox);

                if (c.getParentId() == null) {
                    // Commento principale → lo aggiungiamo direttamente alla sezione
                    commentSection.getChildren().add(commentBox);
                } else {
                    // Commento di risposta → cerchiamo il genitore e lo aggiungiamo come figlio
                    VBox parentBox = mappaCommenti.get(c.getParentId());
                    if (parentBox != null) {
                        commentBox.setStyle("-fx-padding: 5 0 5 20;"); // indentazione
                        parentBox.getChildren().add(commentBox);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errore = new Label("Errore nel caricamento dei commenti.");
            errore.setStyle("-fx-text-fill: red;");
            commentSection.getChildren().add(errore);
        }
    }

    private VBox creaBoxCommento(Commento c) {
        VBox box = new VBox();
        box.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10; -fx-background-radius: 5;");
        box.setSpacing(5);

        Label autore = new Label(c.getAutore());
        autore.setStyle("-fx-font-weight: bold; -fx-text-fill: #dddddd;");

        Label testo = new Label(c.getTesto());
        testo.setWrapText(true);
        testo.setStyle("-fx-text-fill: white;");

        Label data = new Label(c.getData().toString());
        data.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");

        Button rispondiBtn = new Button("Rispondi");
        rispondiBtn.setStyle("-fx-font-size: 10px;");
        rispondiBtn.setOnAction(e -> {
            newCommentField.setPromptText("Rispondi a " + c.getAutore());
            commentoSelezionatoPerRisposta = c.getId();
        });
        box.getStyleClass().add("comment-box");

        box.getChildren().addAll(autore, testo, data, rispondiBtn);
        return box;
    }

    private String formatTime(Duration duration) {
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
