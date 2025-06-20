package com.dashapp.controller;

import com.dashapp.model.*;
import com.dashapp.util.AlertManager;
import com.dashapp.view.ViewNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
import java.util.*;
    /*Gestisce la visualizzazione di un brano, con le sue info, commenti, note e meta-informzioni*/
public class VisualizzaController {

    @FXML private MediaView mediaView;
    @FXML private ImageView audioPlaceholder;
    @FXML private Button toggleButton, forwardButton, rewindButton, scaricaButton;
    @FXML private Slider progressSlider, volumeSlider;
    @FXML private Label timeLabel;
    @FXML private Label titoloLabel;
    @FXML private Label artistaLabel;
    @FXML private Label genereLabel;
    @FXML private Label annoLabel;
    @FXML private VBox commentSection, noteSection;
    @FXML private Button aggiungiNotaButton;
    @FXML private TextField newCommentField;
    @FXML private Button pubblicaButton;
    @FXML private Button commentiButton;
    @FXML private Button noteButton;
    @FXML private ScrollPane commentiPane;
    @FXML private ScrollPane notePane;
    @FXML private VBox videoControls;
    @FXML private StackPane videoContainer;
    @FXML private HBox buttonBar;
    private Timeline fadeOutTimeline;

    private MediaPlayer mediaPlayer;
    private final BranoBean brano = ViewNavigator.getBrano();
    private final CommentoDao commentoDao = new CommentoDao();
    private final NotaDao notaDao = new NotaDao();
    private Integer commentoSelezionatoPerRisposta = null;
    private boolean branoTerminato = false;
    private VisualizzaDao vis = new VisualizzaDao();
    private final Map<Integer, VBox> commentBoxes = new HashMap<>();

    @FXML
    public void initialize() {
            audioPlaceholder.fitWidthProperty().bind(videoContainer.widthProperty());
        setupMedia();
        caricaCommenti();
        caricaNote();
        mostraCommenti();
        vis.carica();
        // Inizializza i controlli video con opacità 0 (nascosti)
        videoControls.setOpacity(0.0);

        // Riduci overlay scuro (opzionale)
        videoContainer.setStyle("-fx-background-color: transparent;");

        commentiButton.setOnAction(e -> mostraCommenti());
        noteButton.setOnAction(e -> mostraNote());

        pubblicaButton.setOnAction(e -> pubblicaCommento());
        aggiungiNotaButton.setOnAction(e -> apriModalInserimentoNota());
        scaricaButton.setOnAction(e -> gestisciDowload());

        titoloLabel.setText(brano.getTitolo()+" caricato da "+vis.getUtente(brano.getUtente()));
        artistaLabel.setText("Autori: "+brano.getAutori());
        genereLabel.setText("Genere: "+brano.getGenere().toString());
        annoLabel.setText("Anno: "+ (brano.getAnno() == null ? "Non conosciuto":brano.getAnno()));//NB : anno può essere sconosciuto
    }

        private void gestisciDowload() {
            Stage stage = (Stage) scaricaButton.getScene().getWindow();
            int res = vis.scaricaBrano(stage, brano.getFile());
            if (res == 1)
                AlertManager.showConfirmation("Brano "+brano.getTitolo()+" caricato con successo");
            else if (res == -1)
                AlertManager.showError("Errore nello scaricamento del brano "+brano.getTitolo());
        }

        private void showControls() {
        if (fadeOutTimeline != null) {
            fadeOutTimeline.stop();
        }
        videoControls.setOpacity(1.0);
    }

    private void startFadeOut() {
        if (fadeOutTimeline != null) {
            fadeOutTimeline.stop();
        }
        fadeOutTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(videoControls.opacityProperty(), 0.0))
        );
        fadeOutTimeline.play();
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
            File mediaFile = new File(file);

            if (!mediaFile.exists()) {
                System.out.println("File non trovato: " + file);
                return;
            }

            String uri = mediaFile.toURI().toString();
            String lower = file.toLowerCase();

            boolean isAudio = lower.endsWith(".mp3") || lower.endsWith(".wav");
            boolean isVideo = lower.endsWith(".mp4") || lower.endsWith(".m4v") || lower.endsWith(".mov");
            boolean isImage = lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif");

            // Gestione immagine
            if (isImage) {
                System.out.println("Caricamento immagine da: " + uri);
                audioPlaceholder.setImage(new Image(uri));
                audioPlaceholder.setVisible(true);
                audioPlaceholder.setManaged(true);

                mediaView.setVisible(false);
                mediaView.setManaged(false);
                return; // Esci subito, niente MediaPlayer
            }
            else if (isVideo || isAudio) {
                // Se non è immagine, reset imageView

                videoContainer.setOnMouseEntered(e -> showControls());
                videoContainer.setOnMouseMoved(e -> showControls());
                videoContainer.setOnMouseExited(e -> startFadeOut());

                audioPlaceholder.setVisible(false);
                audioPlaceholder.setManaged(false);

                // Imposta visibilità dinamica
                mediaView.setVisible(isVideo);
                mediaView.setManaged(isVideo);

                audioPlaceholder.setVisible(isAudio);
                audioPlaceholder.setManaged(isAudio);

                // Se audio, mostra placeholder
                if (isAudio) {
                    InputStream imgStream = getClass().getResourceAsStream("/resources/images/placeholder.gif");
                    if (imgStream != null) {
                        audioPlaceholder.setImage(new Image(imgStream));
                    }
                }

                mediaView.setPreserveRatio(true);
                mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
                mediaView.fitHeightProperty().bind(videoContainer.heightProperty());

                try {
                    Media media = new Media(uri);
                    mediaPlayer = new MediaPlayer(media);
                    ViewNavigator.setRisorsa(mediaPlayer);

                    mediaView.setMediaPlayer(mediaPlayer);

                    // Gestione slider e pulsanti come prima
                    progressSlider.maxProperty().bind(
                            Bindings.createDoubleBinding(
                                    () -> mediaPlayer.getTotalDuration() != null ? mediaPlayer.getTotalDuration().toSeconds() : 0,
                                    mediaPlayer.totalDurationProperty()
                            )
                    );

                    mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                        if (!progressSlider.isValueChanging()) {
                            progressSlider.setValue(newTime.toSeconds());
                        }
                    });

                    progressSlider.setOnMouseReleased(e ->
                            mediaPlayer.seek(Duration.seconds(progressSlider.getValue()))
                    );

                    mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty());

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

                    rewindButton.setOnAction(e ->
                            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5))));
                    forwardButton.setOnAction(e ->
                            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5))));

                    timeLabel.textProperty().bind(Bindings.createStringBinding(() ->
                                    formatTime(mediaPlayer.getCurrentTime()) + " / " +
                                            formatTime(mediaPlayer.getTotalDuration()),
                            mediaPlayer.currentTimeProperty(),
                            mediaPlayer.totalDurationProperty()));

                    mediaPlayer.setOnReady(() -> {
                        toggleButton.setText("⏸");
                        mediaPlayer.play();
                    });

                    mediaPlayer.setOnEndOfMedia(() -> {
                        toggleButton.setText("▶");
                        branoTerminato = true;
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }



        private String formatTime(Duration d) {
        if(d == null)
            return "00:00";
        else {
            int seconds = (int) d.toSeconds();
            return String.format("%02d:%02d", seconds / 60, seconds % 60);
        }
    }

    @FXML
    private void pubblicaCommento() {
        String testo = newCommentField.getText().trim();
        if (testo.isEmpty()) return;

        try {
            commentoDao.aggiungiCommento(brano.getId(), ViewNavigator.getUtenteId(), testo, commentoSelezionatoPerRisposta);
            newCommentField.clear();
            commentoSelezionatoPerRisposta = null;
            caricaCommenti();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void caricaCommenti() {
        commentSection.getChildren().clear();
        commentBoxes.clear();

        try {
            List<CommentoBean> listaCommenti = commentoDao.getCommentiByBranoId(brano.getId());
            Map<Integer, List<CommentoBean>> repliesMap = new HashMap<>();

            for (CommentoBean c : listaCommenti) {
                if (c.getParentId() != null) {
                    repliesMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
                }
            }

            for (CommentoBean c : listaCommenti) {
                if (c.getParentId() == null) {
                    VBox commentBox = creaBoxCommento(c, repliesMap);
                    commentSection.getChildren().add(commentBox);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox creaBoxCommento(CommentoBean commentoBean, Map<Integer, List<CommentoBean>> repliesMap) {
        VBox commentBox = new VBox();
        commentBox.getStyleClass().add("comment-box");
        commentBox.setPadding(new Insets(8));
        commentBox.setSpacing(5);
        commentBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // Nome autore
        Label autoreLabel = new Label(commentoBean.getAutore());
        autoreLabel.getStyleClass().add("comment-author");

        // Testo commento
        Label testoLabel = new Label(commentoBean.getTesto());
        testoLabel.getStyleClass().add("comment-text");
        testoLabel.setWrapText(true);
        commentBox.getChildren().addAll(
                autoreLabel,
                testoLabel
        );
        if (commentoBean.getAutoreId()!=ViewNavigator.getUtenteId()) {
            Button rispondiButton = new Button("Rispondi");
            rispondiButton.getStyleClass().add("reply-button");

            rispondiButton.setOnAction(e -> {
                commentoSelezionatoPerRisposta = commentoBean.getId();
                newCommentField.requestFocus();
                newCommentField.setPromptText("Rispondi a " + commentoBean.getAutore());
            });

            commentBox.getChildren().add(rispondiButton);
        }

        // Container per le risposte
        VBox repliesContainer = new VBox();
        repliesContainer.setSpacing(5);
        repliesContainer.setPadding(new Insets(5, 0, 0, 20));

        List<CommentoBean> replies = repliesMap.get(commentoBean.getId());

        // Bottone toggle risposte (inizialmente nullo)
        Button toggleRepliesButton;

        if (replies != null && !replies.isEmpty()) {
            toggleRepliesButton = new Button("+" + replies.size() + " Risposte");
            toggleRepliesButton.getStyleClass().add("toggle-replies-btn");

            toggleRepliesButton.setOnAction(e -> {
                boolean isVisible = repliesContainer.isVisible();
                repliesContainer.setVisible(!isVisible);
                repliesContainer.setManaged(!isVisible);
                toggleRepliesButton.setText(isVisible ? ("+" + replies.size() + " Risposte") : "- Riduci Risposte");
            });

            for (CommentoBean reply : replies) {
                VBox replyBox = creaBoxCommento(reply, repliesMap);
                repliesContainer.getChildren().add(replyBox);
            }

            repliesContainer.setVisible(false);
            repliesContainer.setManaged(false);
        } else {
            toggleRepliesButton = null;
        }

        // Ora aggiungiamo gli elementi nell’ordine giusto

        if (toggleRepliesButton != null)
            commentBox.getChildren().add(toggleRepliesButton);

        commentBox.getChildren().add(repliesContainer);

        commentBoxes.put(commentoBean.getId(), commentBox);

        return commentBox;
    }


    private void caricaNote() {
        noteSection.getChildren().clear();
        List<NotaBean> note = notaDao.getNotePerBrano(brano.getId());

        for (NotaBean n : note) {
            VBox card = new VBox(8);  // Spazio verticale maggiore
            card.getStyleClass().add("note-card");
            card.setPadding(new Insets(10));
            card.setMaxWidth(400);
            card.setMinWidth(300);

            // Titolo con segmento
            Label segmentLabel = new Label("Segmento: " + n.getSegmentoInizio() + " - " + n.getSegmentoFine());
            segmentLabel.getStyleClass().add("note-segment");

            // Testo libero
            Label testoLabel = new Label(n.getTestoLibero());
            testoLabel.getStyleClass().add("note-text");

            // Data e luogo
            Label infoLabel = new Label("Registrata il: " + n.getDataRegistrazione() +
                    " presso " + n.getLuogoRegistrazione());
            infoLabel.getStyleClass().add("note-info");

            // Strumenti
            Label strumentiLabel = new Label("Strumenti: " +
                    String.join(", ", n.getStrumenti()));
            strumentiLabel.getStyleClass().add("note-extra");
            // Esecutori
            Label esecutoriLabel = new Label("Esecutori: " +
                    String.join(", ", n.getEsecutori()));
            esecutoriLabel.getStyleClass().add("note-extra");

            // Aggiungi tutto alla card
            card.getChildren().addAll(segmentLabel, testoLabel, infoLabel, strumentiLabel, esecutoriLabel);

            noteSection.getChildren().add(card);
        }
    }

    private void apriModalInserimentoNota() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/nota.fxml"));
            Parent root = loader.load();
            NotaController controller = loader.getController();
            controller.setBrano(brano);

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
