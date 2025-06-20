package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.CatalogoDao;
import com.dashapp.view.ViewNavigator;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;

import java.util.*;
import java.util.function.Function;

public class CatalogoController {

    @FXML private VBox sidebar;
    @FXML private Button filterButton;
    @FXML private BorderPane root;
    @FXML private VBox braniContainer;
    @FXML private CheckComboBox<String> aut, ese, str;
    @FXML private JFXTextField tit, gen;

    private boolean sidebarVisible = false;
    private ObservableList<BranoBean> allBrani;
    private Map<String, List<String>> autori, esecutori, strumenti;
    private List<String> filtriAutori = new ArrayList<>();
    private List<String> filtriEsecutori = new ArrayList<>();
    private List<String> filtriStrumenti = new ArrayList<>();
    private String filtroTitolo = "", filtroGenere = "";

    @FXML
    public void initialize() {
        root.setLeft(null);

        CatalogoDao cat = new CatalogoDao();
        List<BranoBean> listaBrani = cat.getBrani();

        autori = cat.getDizionarioAutori();
        esecutori = cat.getDizionarioEsecutori();
        strumenti = cat.getDizionarioStrumenti();

        allBrani = FXCollections.observableArrayList(listaBrani);

        setupFiltroText(tit, s -> filtroTitolo = s);
        setupFiltroText(gen, s -> filtroGenere = s);

        setupFiltroCombo(aut, autori, selected -> {
            filtriAutori = selected;
            updateFiltroGlobale();
        });

        setupFiltroCombo(ese, esecutori, selected -> {
            filtriEsecutori = selected;
            updateFiltroGlobale();
        });

        setupFiltroCombo(str, strumenti, selected -> {
            filtriStrumenti = selected;
            updateFiltroGlobale();
        });

        updateFiltroGlobale();
    }

    private void setupFiltroText(TextField field, java.util.function.Consumer<String> consumer) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            consumer.accept(newVal.toLowerCase());
            updateFiltroGlobale();
        });
    }

    private void setupFiltroCombo(CheckComboBox<String> comboBox,
                                  Map<String, List<String>> dizionario,
                                  java.util.function.Consumer<List<String>> consumer) {
        List<String> items = dizionario.values().stream()
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .toList();
        comboBox.getItems().setAll(items);

        comboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
            consumer.accept(new ArrayList<>(comboBox.getCheckModel().getCheckedItems()));
        });
    }

    private void updateFiltroGlobale() {
        braniContainer.getChildren().clear();

        List<BranoBean> filtrati = allBrani.stream().filter(brano -> {

            if (!filtroTitolo.isEmpty() && !brano.getTitolo().toLowerCase().contains(filtroTitolo))
                return false;

            if (!filtroGenere.isEmpty() && !brano.getGenere2().toLowerCase().contains(filtroGenere))
                return false;

            if (!filtriAutori.isEmpty()) {
                List<String> autoriBrano = autori.getOrDefault(brano.getTitolo(), List.of());
                if (!autoriBrano.containsAll(filtriAutori)) return false;
            }

            if (!filtriEsecutori.isEmpty()) {
                List<String> esecutoriBrano = esecutori.getOrDefault(brano.getTitolo(), List.of());
                if (!esecutoriBrano.containsAll(filtriEsecutori)) return false;
            }

            if (!filtriStrumenti.isEmpty()) {
                List<String> strumentiBrano = strumenti.getOrDefault(brano.getTitolo(), List.of());
                if (!strumentiBrano.containsAll(filtriStrumenti)) return false;
            }

            return true;

        }).toList();

        if (filtrati.isEmpty()) {
            Label noResults = new Label("Nessun brano trovato");
            noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: grey; -fx-alignment: center;");
            braniContainer.getChildren().add(noResults);
        } else {
            filtrati.forEach(this::aggiungiBrano);
        }
    }

    @FXML
    private void toggleSidebar() {
        if (sidebarVisible) {
            TranslateTransition transition = new TranslateTransition(Duration.millis(200), sidebar);
            transition.setFromX(0);
            transition.setToX(-sidebar.getWidth());
            transition.setOnFinished(e -> root.setLeft(null));
            transition.play();
        } else {
            root.setLeft(sidebar);
            sidebar.setTranslateX(-sidebar.getWidth());
            TranslateTransition transition = new TranslateTransition(Duration.millis(200), sidebar);
            transition.setFromX(-sidebar.getWidth());
            transition.setToX(0);
            transition.play();
        }
        sidebarVisible = !sidebarVisible;
    }

    public void aggiungiBrano(BranoBean b) {
        HBox hbox = new HBox(15);
        hbox.getStyleClass().add("brano-card");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPrefHeight(110);
        hbox.setPrefWidth(650);
        hbox.setPadding(new Insets(15));

        VBox vbox = new VBox(6);
        vbox.setAlignment(Pos.CENTER_LEFT);

        Text titoloText = new Text(b.getTitolo());
        titoloText.getStyleClass().add("brano-title");

        Text autoreText = new Text("Autore: " + b.getAutori() + " • " + b.getTipo());
        autoreText.getStyleClass().add("brano-meta");

        Text dettagliText = new Text(b.getGenere() + " • " + (b.getAnno() == null ? "Anno non conosciuto" : b.getAnno()));
        dettagliText.getStyleClass().add("brano-meta");

        vbox.getChildren().addAll(titoloText, autoreText, dettagliText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        JFXButton playButton = new JFXButton("▶");
        playButton.getStyleClass().add("circular-button");

        playButton.setOnAction(e -> {
            System.out.println("Riproduzione: " + b.getFile());
            ViewNavigator.setBrano(b);
            if (b.getFile().startsWith("http")) {
                ViewNavigator.setLink(b.getFile());
                ViewNavigator.navigateToWebView();
            } else {
                ViewNavigator.navigateToBrano();
            }
        });

        hbox.getChildren().addAll(vbox, spacer, playButton);
        braniContainer.getChildren().add(hbox);
    }
}
