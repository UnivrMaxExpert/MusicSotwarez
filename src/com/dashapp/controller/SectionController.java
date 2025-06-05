package com.dashapp.controller;

import com.dashapp.model.*;
import com.dashapp.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class SectionController implements Initializable {

    @FXML private VBox root;
    @FXML private VBox commentSection, noteSection;
    @FXML private Button aggiungiNotaButton;
    @FXML private TextField newCommentField;
    @FXML private Button pubblicaButton;
    @FXML private Button commentiButton;
    @FXML private Button noteButton;
    @FXML private ScrollPane commentiPane;
    @FXML private ScrollPane notePane;
    @FXML private Label titoloLabel;
    @FXML private Label artistaLabel;
    @FXML private Label genereLabel;
    @FXML private Label annoLabel;

    private final Map<Integer, VBox> commentBoxes = new HashMap<>();
    private final CommentoDao commentoDao = new CommentoDao();
    private final BranoBean brano = ViewNavigator.getBrano();
    private final NotaDao notaDao = new NotaDao();
    private Integer commentoSelezionatoPerRisposta = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        caricaCommenti();
        caricaNote();
        mostraCommenti();

        commentiButton.setOnAction(e -> mostraCommenti());
        noteButton.setOnAction(e -> mostraNote());
        pubblicaButton.setOnAction(e -> pubblicaCommento());
        aggiungiNotaButton.setOnAction(e -> apriModalInserimentoNota());

        titoloLabel.setText(brano.getTitolo());
        artistaLabel.setText("Autori: " + brano.getAutori());
        genereLabel.setText("Genere: " + brano.getGenere());
        annoLabel.setText("Anno: " + brano.getAnno());
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
            mostraErrore("Errore durante la pubblicazione del commento.", e);
        }
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
            mostraErrore("Errore durante il caricamento dei commenti.", e);
        }
    }

    private VBox creaBoxCommento(Commento commento, Map<Integer, List<Commento>> repliesMap) {
        VBox commentBox = new VBox(5);
        commentBox.getStyleClass().add("comment-box");
        commentBox.setPadding(new Insets(8));
        commentBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Label autoreLabel = new Label(commento.getAutore());
        autoreLabel.getStyleClass().add("comment-author");

        Label testoLabel = new Label(commento.getTesto());
        testoLabel.getStyleClass().add("comment-text");
        testoLabel.setWrapText(true);

        VBox repliesContainer = new VBox(5);
        repliesContainer.setPadding(new Insets(5, 0, 0, 20));

        List<Commento> replies = repliesMap.get(commento.getId());
        Button toggleRepliesButton;

        if (replies != null && !replies.isEmpty()) {
            toggleRepliesButton = new Button("+" + replies.size() + " Risposte");
            toggleRepliesButton.getStyleClass().add("toggle-replies-btn");

            for (Commento reply : replies) {
                VBox replyBox = creaBoxCommento(reply, repliesMap);
                repliesContainer.getChildren().add(replyBox);
            }

            repliesContainer.setVisible(false);
            repliesContainer.setManaged(false);

            toggleRepliesButton.setOnAction(e -> {
                boolean isVisible = repliesContainer.isVisible();
                repliesContainer.setVisible(!isVisible);
                repliesContainer.setManaged(!isVisible);
                toggleRepliesButton.setText(isVisible ? ("+" + replies.size() + " Risposte") : "- Riduci Risposte");
            });
        } else {
            toggleRepliesButton = null;
        }

        commentBox.getChildren().addAll(autoreLabel, testoLabel);
        if (toggleRepliesButton != null) commentBox.getChildren().add(toggleRepliesButton);
        commentBox.getChildren().add(repliesContainer);

        commentBoxes.put(commento.getId(), commentBox);
        return commentBox;
    }

    private void caricaNote() {
        noteSection.getChildren().clear();
        List<NotaBean> note = notaDao.getNotePerBrano(brano.getId());
        for (NotaBean n : note) {
            VBox card = new VBox(8);
            card.getStyleClass().add("note-card");
            card.setPadding(new Insets(10));
            card.setMaxWidth(400);
            card.setMinWidth(300);

            Label segmentLabel = new Label("Segmento: " + n.getSegmentoInizio() + " - " + n.getSegmentoFine());
            segmentLabel.getStyleClass().add("note-segment");

            Label testoLabel = new Label(n.getTestoLibero());
            testoLabel.getStyleClass().add("note-text");

            Label infoLabel = new Label("Registrata il: " + n.getDataRegistrazione() +
                    " presso " + n.getLuogoRegistrazione());
            infoLabel.getStyleClass().add("note-info");

            Label strumentiLabel = new Label("Strumenti: " + String.join(", ", n.getStrumenti()));
            strumentiLabel.getStyleClass().add("note-extra");

            Label esecutoriLabel = new Label("Esecutori: " + String.join(", ", n.getEsecutori()));
            esecutoriLabel.getStyleClass().add("note-extra");

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
            mostraErrore("Errore durante l'apertura della finestra di inserimento nota.", e);
        }
    }

    private void mostraErrore(String messaggio, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(messaggio);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }
}
