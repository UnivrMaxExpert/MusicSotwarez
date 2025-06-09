package com.dashapp.controller;

import com.dashapp.model.*;
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

    private final Map<Integer, VBox> commentBoxes = new HashMap<>();

    @FXML
    public void initialize() {
        audioPlaceholder.fitWidthProperty().bind(videoContainer.widthProperty());
        setupMedia();
        caricaCommenti();
        caricaNote();
        mostraCommenti();

        // Inizializza i controlli video con opacità 0 (nascosti)
        videoControls.setOpacity(0.0);

        // Riduci overlay scuro (opzionale)
        videoContainer.setStyle("-fx-background-color: transparent;");

        // Gestisci comparsa dei controlli
        videoContainer.setOnMouseEntered(e -> showControls());
        videoContainer.setOnMouseMoved(e -> showControls());
        videoContainer.setOnMouseExited(e -> startFadeOut());

        commentiButton.setOnAction(e -> mostraCommenti());
        noteButton.setOnAction(e -> mostraNote());

        pubblicaButton.setOnAction(e -> pubblicaCommento());
        aggiungiNotaButton.setOnAction(e -> apriModalInserimentoNota());

        titoloLabel.setText(brano.getTitolo());
        artistaLabel.setText("Autori: "+brano.getAutori());
        genereLabel.setText("Genere: "+brano.getGenere().toString());
        annoLabel.setText("Anno: "+ brano.getAnno());//NB : anno può essere sconosciuto
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
        File videoFile = new File(file);

        if (!videoFile.exists()) {
            System.out.println("File non trovato: " + file);
            return;
        }

        String uri = Paths.get(file).toUri().toString();
        boolean isAudio = file.toLowerCase().endsWith(".mp3") || file.toLowerCase().endsWith(".wav");

        mediaView.setVisible(!isAudio);
        mediaView.setManaged(!isAudio);
        audioPlaceholder.setVisible(isAudio);
        audioPlaceholder.setManaged(isAudio);

        mediaView.setPreserveRatio(true);

        // Bind MediaView dinamico al container
        mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
        mediaView.fitHeightProperty().bind(videoContainer.heightProperty());

        if (isAudio) {
            InputStream imgStream = getClass().getResourceAsStream("/resources/images/placeholder.gif");
            if (imgStream != null) {
                audioPlaceholder.setImage(new Image(imgStream));
            }
        }

        try {
            Media media = new Media(uri);
            mediaPlayer = new MediaPlayer(media);
            ViewNavigator.setRisorsa(mediaPlayer);
            mediaView.setMediaPlayer(mediaPlayer);

            // Binding dinamico slider progress
            progressSlider.maxProperty().bind(
                    Bindings.createDoubleBinding(
                            () -> mediaPlayer.getTotalDuration() != null ? mediaPlayer.getTotalDuration().toSeconds() : 0,
                            mediaPlayer.totalDurationProperty()
                    )
            );

            // Ascolta variazione tempo corrente
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                }
            });

            // Se slider cambia, aggiorna il video
            progressSlider.setOnMouseReleased(e ->
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()))
            );

            // Volume slider binding
            mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty());

            // Play/Pause button
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

            // Formatta tempo dinamico con Binding
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
            List<Commento> listaCommenti = commentoDao.getCommentiByBranoId(brano.getId());
            Map<Integer, List<Commento>> repliesMap = new HashMap<>();

            for (Commento c : listaCommenti) {
                if (c.getParentId() != null) {
                    repliesMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
                }
            }

            for (Commento c : listaCommenti) {
                if (c.getParentId() == null) {
                    VBox commentBox = creaBoxCommento(c, repliesMap);
                    commentSection.getChildren().add(commentBox);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox creaBoxCommento(Commento commento, Map<Integer, List<Commento>> repliesMap) {
        VBox commentBox = new VBox();
        commentBox.getStyleClass().add("comment-box");
        commentBox.setPadding(new Insets(8));
        commentBox.setSpacing(5);
        commentBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // Nome autore
        Label autoreLabel = new Label(commento.getAutore());
        autoreLabel.getStyleClass().add("comment-author");

        // Testo commento
        Label testoLabel = new Label(commento.getTesto());
        testoLabel.getStyleClass().add("comment-text");
        testoLabel.setWrapText(true);

        // Container per le risposte
        VBox repliesContainer = new VBox();
        repliesContainer.setSpacing(5);
        repliesContainer.setPadding(new Insets(5, 0, 0, 20));

        List<Commento> replies = repliesMap.get(commento.getId());

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

            for (Commento reply : replies) {
                VBox replyBox = creaBoxCommento(reply, repliesMap);
                repliesContainer.getChildren().add(replyBox);
            }

            repliesContainer.setVisible(false);
            repliesContainer.setManaged(false);
        } else {
            toggleRepliesButton = null;
        }

        // Ora aggiungiamo gli elementi nell’ordine giusto
        commentBox.getChildren().addAll(
                autoreLabel,
                testoLabel
        );

        if (toggleRepliesButton != null) {
            commentBox.getChildren().add(toggleRepliesButton);
        }

        commentBox.getChildren().add(repliesContainer);

        commentBoxes.put(commento.getId(), commentBox);

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
            //n.getStrumenti().forEach(System.out::println);
            //n.getEsecutori().forEach(System.out::println);
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
