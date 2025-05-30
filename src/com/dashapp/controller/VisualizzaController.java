package com.dashapp.controller;

import com.dashapp.model.*;
import com.dashapp.view.ViewNavigator;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizzaController {

    @FXML private MediaView mediaView;
    @FXML private ImageView audioPlaceholder;
    @FXML private Button toggleButton, forwardButton, rewindButton, scaricaButton;
    @FXML private Slider progressSlider, volumeSlider;
    @FXML private Label timeLabel;
    @FXML private VBox commentSection, noteSection;
    @FXML private Button aggiungiNotaButton;
    @FXML private JFXTextField newCommentField;
    @FXML private Button pubblicaButton;
    @FXML private Button commentiButton;
    @FXML private Button noteButton;
    @FXML private ScrollPane commentiPane;
    @FXML private ScrollPane notePane;
    @FXML private VBox videoControls;
    @FXML private StackPane videoContainer;

    private MediaPlayer mediaPlayer;
    private final BranoBean brano = ViewNavigator.getBrano();
    private final CommentoDao commentoDao = new CommentoDao();
    private final NotaDao notaDao = new NotaDao();
    private Integer commentoSelezionatoPerRisposta = null;
    private boolean branoTerminato = false;

    @FXML
    public void initialize() {
        setupMedia();
        caricaCommenti();
        caricaNote();
        mostraCommenti();

        videoControls.setVisible(false);
        videoControls.setManaged(false);

        videoContainer.setOnMouseEntered(e -> {
            videoControls.setVisible(true);
            videoControls.setManaged(true);
        });

        videoContainer.setOnMouseExited(e -> {
            videoControls.setVisible(false);
            videoControls.setManaged(false);
        });

        commentiButton.setOnAction(e -> mostraCommenti());
        noteButton.setOnAction(e -> mostraNote());

        pubblicaButton.setOnAction(e -> pubblicaCommento());
        aggiungiNotaButton.setOnAction(e -> apriModalInserimentoNota());
    }

    @FXML
    private void mostraCommenti() {
        commentiPane.setVisible(true);
        commentiPane.setManaged(true);
        notePane.setVisible(false);
        notePane.setManaged(false);

        commentiButton.getStyleClass().add("active-tab");
        noteButton.getStyleClass().remove("active-tab");
    }

    @FXML
    private void mostraNote() {
        commentiPane.setVisible(false);
        commentiPane.setManaged(false);
        notePane.setVisible(true);
        notePane.setManaged(true);

        noteButton.getStyleClass().add("active-tab");
        commentiButton.getStyleClass().remove("active-tab");
    }

    private void setupMedia() {
        String file = brano.getFile();
        File videoFile = new File(file);

        if (!videoFile.exists()) {
            System.out.println("File non trovato: " + file);
            return;
        }

        String uri = Paths.get(file).toUri().toString();
        System.out.println("Percorso video generato: " + uri);

        boolean isAudio = file.toLowerCase().endsWith(".mp3") || file.toLowerCase().endsWith(".wav");

        mediaView.setVisible(!isAudio);
        mediaView.setManaged(!isAudio);
        audioPlaceholder.setVisible(isAudio);
        audioPlaceholder.setManaged(isAudio);

        // Imposta rapporto 16:9
        mediaView.setPreserveRatio(true);
        mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
        mediaView.fitHeightProperty().bind(
                mediaView.fitWidthProperty().divide(16.0/9.0)
        );

        audioPlaceholder.fitWidthProperty().bind(videoContainer.widthProperty());
        audioPlaceholder.fitHeightProperty().bind(
                audioPlaceholder.fitWidthProperty().divide(16.0/9.0)
        );

        if (isAudio) {
            InputStream imgStream = getClass().getResourceAsStream("/resources/images/placeholder.gif");
            if (imgStream != null) {
                audioPlaceholder.setImage(new Image(imgStream));
            } else {
                System.out.println("Immagine placeholder non trovata.");
            }
        }

        try {
            Media media = new Media(uri);
            media.setOnError(() -> System.out.println("Errore Media: " + media.getError().getMessage()));

            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnError(() -> System.out.println("Errore MediaPlayer: " + mediaPlayer.getError().getMessage()));
            mediaView.setMediaPlayer(mediaPlayer);

            mediaPlayer.setOnReady(() -> {
                progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                mediaPlayer.setVolume(volumeSlider.getValue());
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                branoTerminato = true;
                toggleButton.setText("▶");
            });

            toggleButton.setOnAction(e -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                Duration totalDuration = mediaPlayer.getTotalDuration();

                if (currentTime.greaterThanOrEqualTo(totalDuration.subtract(Duration.seconds(0.3)))) {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                    toggleButton.setText("⏸");
                    return;
                }

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
                Duration cur = mediaPlayer.getCurrentTime();
                mediaPlayer.seek(cur.subtract(Duration.seconds(5)).greaterThan(Duration.ZERO)
                        ? cur.subtract(Duration.seconds(5)) : Duration.ZERO);
            });

            forwardButton.setOnAction(e -> {
                Duration cur = mediaPlayer.getCurrentTime();
                Duration tot = mediaPlayer.getTotalDuration();
                mediaPlayer.seek(cur.add(Duration.seconds(5)).lessThan(tot) ? cur.add(Duration.seconds(5)) : tot);
            });

            mediaPlayer.currentTimeProperty().addListener((o, oldT, newT) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newT.toSeconds());
                }
                String current = formatTime(newT);
                String total = formatTime(mediaPlayer.getTotalDuration());
                timeLabel.setText(current + " / " + total);
            });

            progressSlider.setOnMouseReleased(e -> mediaPlayer.seek(Duration.seconds(progressSlider.getValue())));
            volumeSlider.valueProperty().addListener((o, oldV, newV) -> mediaPlayer.setVolume(newV.doubleValue()));

        } catch (Exception ex) {
            System.out.println("Errore durante la creazione del MediaPlayer: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String formatTime(Duration d) {
        int secs = (int) Math.floor(d.toSeconds());
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    @FXML private void scaricaBrano() {
        if (brano == null || brano.getFile() == null) return;
        String risultato = new CaricaDao().scaricaBrano(
                (Stage) scaricaButton.getScene().getWindow(), brano.getFile()
        );
        if (risultato != null) System.out.println("Download completato: " + risultato);
    }

    private void pubblicaCommento() {
        String testo = newCommentField.getText().trim();
        if (testo.isEmpty()) return;
        try {
            int uid = ViewNavigator.getUtenteId();
            commentoDao.aggiungiCommento(brano.getId(), uid, testo, commentoSelezionatoPerRisposta);
            newCommentField.clear();
            commentoSelezionatoPerRisposta = null;
            caricaCommenti();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void caricaCommenti() {
        commentSection.getChildren().clear();
        try {
            List<Commento> list = commentoDao.getCommentiByBranoId(brano.getId());
            Map<Integer, VBox> mappa = new HashMap<>();
            for (Commento c : list) {
                VBox box = creaBoxCommento(c);
                mappa.put(c.getId(), box);
                if (c.getParentId() == null) {
                    commentSection.getChildren().add(box);
                } else {
                    VBox parent = mappa.get(c.getParentId());
                    if (parent != null) {
                        box.setStyle("-fx-padding:5 0 5 20;");
                        parent.getChildren().add(box);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            commentSection.getChildren().add(new Label("Errore caricamento commenti"));
        }
    }

    private VBox creaBoxCommento(Commento c) {
        VBox commentBox = new VBox(5);
        commentBox.getStyleClass().add("comment-box");

        Label autoreLabel = new Label(c.getAutore());
        autoreLabel.getStyleClass().add("comment-author");

        Label testoLabel = new Label(c.getTesto());
        testoLabel.setWrapText(true);

        Label dataLabel = new Label(c.getData().toString());
        dataLabel.getStyleClass().add("comment-date");

        Button rispondiButton = new Button("Rispondi");
        rispondiButton.getStyleClass().add("reply-button");

        VBox replyContainer = new VBox(5);
        replyContainer.setVisible(false);
        replyContainer.setManaged(false);

        if (c.getAutoreId() == ViewNavigator.getUtenteId()) {
            rispondiButton.setDisable(true);
            rispondiButton.setText("Non puoi rispondere a te stesso");
            rispondiButton.setStyle("-fx-opacity: 0.6; -fx-cursor: default;");
        } else {
            rispondiButton.setOnAction(e -> {
                boolean isVisible = replyContainer.isVisible();
                replyContainer.setVisible(!isVisible);
                replyContainer.setManaged(!isVisible);
            });
        }

        JFXTextField rispostaField = new JFXTextField();
        rispostaField.setPromptText("Rispondi a " + c.getAutore());
        rispostaField.getStyleClass().add("comm-field");

        Button pubblicaRisposta = new Button("Pubblica");
        Button annullaRisposta = new Button("Annulla");

        pubblicaRisposta.setOnAction(e -> {
            String testoRisposta = rispostaField.getText().trim();
            if (!testoRisposta.isEmpty()) {
                try {
                    int uid = ViewNavigator.getUtenteId();
                    commentoDao.aggiungiCommento(brano.getId(), uid, testoRisposta, c.getId());
                    caricaCommenti();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        annullaRisposta.setOnAction(e -> {
            rispostaField.clear();
            replyContainer.setVisible(false);
            replyContainer.setManaged(false);
        });

        replyContainer.getChildren().addAll(rispostaField, new HBox(10, pubblicaRisposta, annullaRisposta));
        commentBox.getChildren().addAll(autoreLabel, testoLabel, dataLabel, rispondiButton, replyContainer);
        return commentBox;
    }

    private void caricaNote() {
        noteSection.getChildren().clear();
        List<NotaBean> note = notaDao.getNotePerBrano(brano.getId());
        for (NotaBean n : note) {
            VBox card = new VBox(6);
            card.getStyleClass().add("comment-box");
            card.getChildren().addAll(
                    new Label("Segmento: " + n.getSegmentoInizio() + " - " + n.getSegmentoFine()) {{
                        getStyleClass().add("comment-author");
                    }},
                    new Label(n.getTestoLibero()) {{
                        setWrapText(true);
                        getStyleClass().add("comment-text");
                    }}
            );
            noteSection.getChildren().add(card);
        }
    }

    private void apriModalInserimentoNota() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/nota.fxml"));
            Parent root = loader.load();
            NotaController ctrl = loader.getController();
            ctrl.setBrano(brano);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Aggiungi Nota");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            caricaNote();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
