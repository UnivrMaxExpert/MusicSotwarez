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
    /*Gestisce la sezione di informazioni del brano, commenti e note di un brano, funziona ma sarebbe da perfezionare*/
public class SectionController implements Initializable {

    @FXML private VBox root;
    @FXML private VBox commentSection, noteSection, metaSection;
    @FXML private Button aggiungiNotaButton, metaButton, aggiungiMetaButton;
    @FXML private TextField newCommentField;
    @FXML private Button pubblicaButton;
    @FXML private Button commentiButton;
    @FXML private Button noteButton;
    @FXML private ScrollPane commentiPane, metaPane;
    @FXML private ScrollPane notePane;
    @FXML private Label titoloLabel;
    @FXML private Label artistaLabel;
    @FXML private Label genereLabel;
    @FXML private Label annoLabel;

    private final Map<Integer, VBox> commentBoxes = new HashMap<>();
    private Map<Integer, String> mapUtenti = new HashMap<>();
    private final CommentoDao commentoDao = new CommentoDao();
    private final BranoBean brano = ViewNavigator.getBrano();
    private final NotaDao notaDao = new NotaDao();
    private Integer commentoSelezionatoPerRisposta = null;
    private VisualizzaDao vis = new VisualizzaDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapUtenti = notaDao.getUtenti();
        mapUtenti.forEach((k, v) -> System.out.println(k + ": " + v));
        if(ViewNavigator.getBrano().isConcerto())
            caricaMeta();
        caricaCommenti();
        caricaNote();
        mostraCommenti();
        vis.carica();

        commentiButton.setOnAction(e -> mostraCommenti());
        noteButton.setOnAction(e -> mostraNote());
        metaButton.setOnAction(e -> mostraMeta());
        pubblicaButton.setOnAction(e -> pubblicaCommento());
        aggiungiNotaButton.setOnAction(e -> apriModalNota());
        aggiungiMetaButton.setOnAction(e -> apriModalMeta());

        titoloLabel.setText(brano.getTitolo()+" caricato da "+vis.getUtente(brano.getUtente()));
        artistaLabel.setText("Autori: " + brano.getAutori());
        genereLabel.setText("Genere: " + brano.getGenere());
        annoLabel.setText("Anno: " + brano.getAnno());
    }

    private void caricaMeta() {
        metaSection.getChildren().clear();
        List<MetaBean> note = notaDao.getMetaPerBrano(brano.getId());
        for (MetaBean n : note) {
            VBox card = new VBox(8);
            card.getStyleClass().add("note-card");
            card.setPadding(new Insets(10));
            card.setMaxWidth(400);
            card.setMinWidth(300);
            System.out.println("Utente: "+n.getId());
            Label titleLabel = new Label("Meta-informazioni di " + mapUtenti.get(n.getIdUtente()));
            titleLabel.getStyleClass().add("note-segment");

            Label segmentLabel = new Label("Inizio: " + n.getSegmentoInizio() + ", Fine: " + n.getSegmentoFine());
            segmentLabel.getStyleClass().add("note-text");

            Label testoLabel = new Label(n.getCommento());
            testoLabel.getStyleClass().add("note-text");

            Label infoLabel = new Label("Registrata il: " + n.getDataRegistrazione());
            infoLabel.getStyleClass().add("note-info");

            Label strumentiLabel = new Label("Strumenti: " + String.join(", ", n.getStrumenti()));
            strumentiLabel.getStyleClass().add("note-extra");

            Label esecutoriLabel = new Label("Esecutori: " + String.join(", ", n.getEsecutori()));
            esecutoriLabel.getStyleClass().add("note-extra");

            card.getChildren().addAll(titleLabel, segmentLabel, testoLabel, infoLabel, strumentiLabel, esecutoriLabel);
            metaSection.getChildren().add(card);
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
            mostraErrore("Errore durante la pubblicazione del commento.", e);
        }
    }

    @FXML
    private void mostraCommenti() {
        commentiPane.setVisible(true);
        commentiPane.setManaged(true);
        notePane.setVisible(false);
        notePane.setManaged(false);
        metaPane.setVisible(false);
        metaPane.setManaged(false);


        commentiButton.getStyleClass().add("active-tab");
        noteButton.getStyleClass().remove("active-tab");
        metaButton.getStyleClass().remove("active-tab");
    }

    @FXML
    private void mostraNote() {
        commentiPane.setVisible(false);
        commentiPane.setManaged(false);
        metaPane.setVisible(false);
        metaPane.setManaged(false);
        notePane.setVisible(true);
        notePane.setManaged(true);

        noteButton.getStyleClass().add("active-tab");
        commentiButton.getStyleClass().remove("active-tab");
        metaButton.getStyleClass().remove("active-tab");
    }

    @FXML
    private void mostraMeta() {
        commentiPane.setVisible(false);
        commentiPane.setManaged(false);
        metaPane.setVisible(true);
        metaPane.setManaged(true);
        notePane.setVisible(false);
        notePane.setManaged(false);

        noteButton.getStyleClass().remove("active-tab");
        commentiButton.getStyleClass().remove("active-tab");
        metaButton.getStyleClass().add("active-tab");
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
            mostraErrore("Errore durante il caricamento dei commenti.", e);
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
            VBox card = new VBox(8);
            card.getStyleClass().add("note-card");
            card.setPadding(new Insets(10));
            card.setMaxWidth(400);
            card.setMinWidth(300);

            Label titleLabel = new Label("Nota di "+(mapUtenti.get(n.getId())));
            titleLabel.getStyleClass().add("note-segment");

            Label segmentLabel = new Label("Segmento: " + n.getSegmentoInizio() + " - " + n.getSegmentoFine());
            segmentLabel.getStyleClass().add("note-text");

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

    private void apriModalNota() {
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
            mostraErrore("Errore durante l'apertura della finestra di inserimento nota", e);
        }
    }

    private void apriModalMeta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/meta.fxml"));
            Parent root = loader.load();
            MetaController controller = loader.getController();
            controller.setBrano(brano);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Aggiungi Meta-informazione");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            caricaNote();
        } catch (IOException e) {
            mostraErrore("Errore durante l'apertura della finestra di inserimento meta-informazione", e);
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
