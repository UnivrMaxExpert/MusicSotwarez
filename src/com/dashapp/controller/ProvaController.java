package com.dashapp.controller; // Assicurati che il package sia corretto

import javafx.fxml.FXML;
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
import java.nio.file.Paths;

public class ProvaController {

    @FXML private StackPane videoContainer;
    @FXML private MediaView mediaView;
    @FXML private VBox videoControls; // Il container dei controlli che vogliamo mostrare/nascondere

    @FXML private Button playPauseButton;
    @FXML private Button rewindButton;
    @FXML private Button forwardButton;
    @FXML private Slider progressSlider;
    @FXML private Label timeLabel;
    @FXML private Slider volumeSlider;

    private MediaPlayer mediaPlayer;
    private boolean controlsVisible = false; // Stato per tracciare la visibilità dei controlli

    @FXML
    public void initialize() {
        // Carica un video di esempio (sostituisci con il tuo percorso)
        String videoPath = "C:/Users/louis/OneDrive/Desktop/MerdavichLeccamiLePalle.mp4"; // Esempio: "C:/Users/YourUser/Videos/my_video.mp4"
        File videoFile = new File(videoPath);

        if (!videoFile.exists()) {
            System.err.println("File video non trovato: " + videoPath);
            // Potresti mostrare un messaggio all'utente o disabilitare il player
            return;
        }

        Media media = new Media(Paths.get(videoFile.getAbsolutePath()).toUri().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        // Imposta i listener per mostrare/nascondere i controlli
        videoContainer.setOnMouseEntered(event -> showControls());
        videoContainer.setOnMouseExited(event -> hideControls());

        // Aggiungi la logica per i pulsanti (play/pause, rewind, forward, slider, volume)
        setupPlayerControls();
    }

    private void setupPlayerControls() {
        // Esempio di gestione del pulsante Play/Pause
        playPauseButton.setOnAction(event -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("▶");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("⏸");
            }
        });

        // Listener per l'aggiornamento del tempo e dello slider
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressSlider.isValueChanging()) {
                progressSlider.setValue(newValue.toSeconds());
            }
            timeLabel.setText(formatTime(newValue) + " / " + formatTime(mediaPlayer.getTotalDuration()));
        });

        // Quando il video è pronto, imposta la durata massima dello slider
        mediaPlayer.setOnReady(() -> {
            progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            timeLabel.setText("00:00 / " + formatTime(mediaPlayer.getTotalDuration()));
        });

        // Drag del progress slider
        progressSlider.setOnMousePressed(event -> mediaPlayer.pause());
        progressSlider.setOnMouseReleased(event -> {
            mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            if (!playPauseButton.getText().equals("▶")) { // Riprende se non era in pausa prima del drag
                mediaPlayer.play();
            }
        });

        // Gestione del volume
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                mediaPlayer.setVolume(newValue.doubleValue())
        );

        // ... aggiungi logica per rewindButton, forwardButton, ecc.
    }

    private void showControls() {
        if (!controlsVisible) {
            videoControls.setVisible(true);
            videoControls.setManaged(true);
            controlsVisible = true;
        }
    }

    private void hideControls() {
        // Puoi aggiungere un ritardo qui per un effetto più "YouTube-like"
        // o nasconderli immediatamente se il mouse esce.
        // Per un ritardo, dovresti usare un Timeline o FadeTransition.
        if (controlsVisible) {
            videoControls.setVisible(false);
            videoControls.setManaged(false);
            controlsVisible = false;
        }
    }

    private String formatTime(Duration d) {
        int secs = (int) Math.floor(d.toSeconds());
        int minutes = secs / 60;
        int remainingSecs = secs % 60;
        return String.format("%02d:%02d", minutes, remainingSecs);
    }
}