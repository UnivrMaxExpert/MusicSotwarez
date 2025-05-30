package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.CaricaDao;
import com.dashapp.model.Genere;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.jfoenix.controls.JFXTextField;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CaricaController implements Initializable {

    @FXML private Button btn;
    @FXML private ComboBox<Genere> menugenre;
    @FXML private TextField titolo;
    @FXML private Button add;
    @FXML private VBox vboxContainer;
    @FXML private Spinner<Integer> anno;
    @FXML private Label fileLabel;
    @FXML private Label statusLabel;
    @FXML private JFXTextField youtubeLink;

    private String path;
    private final CaricaDao caricaDao = new CaricaDao();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Popola ComboBox con enum Genere
        ObservableList<Genere> generiList = FXCollections.observableArrayList(Genere.values());
        menugenre.setItems(generiList);
        menugenre.setValue(Genere.HIP_HOP); // valore di default

        // Spinner per anni
        SpinnerValueFactory<Integer> yearFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2025, 2025);
        anno.setValueFactory(yearFactory);
        anno.setEditable(true);

        // Label iniziale
        fileLabel.setText("Nessun file selezionato");
        fileLabel.setStyle("-fx-text-fill: gray;");
        statusLabel.setVisible(false);
    }

    @FXML
    private void handleAdd() {
        HBox currentBox = (HBox) add.getParent();
        currentBox.getChildren().remove(add);

        HBox newHBox = new HBox(15);
        newHBox.setAlignment(Pos.CENTER_LEFT);
        newHBox.setSpacing(15);

        Text newText = new Text("Autori:");
        newText.getStyleClass().add("label");
        newText.setVisible(false);
        TextField newTextField = new TextField();
        newTextField.setPrefWidth(250);

        newHBox.getChildren().addAll(newText, newTextField, add);
        vboxContainer.getChildren().add(newHBox);

        newHBox.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), newHBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    public void openFileChooser() {
        Stage stage = (Stage) btn.getScene().getWindow();
        path = caricaDao.openfilechooser(stage);

        if (path != null) {
            fileLabel.setText(new File(path).getName());
            fileLabel.setStyle("-fx-text-fill: green;");
        } else {
            fileLabel.setText("Nessun file selezionato");
            fileLabel.setStyle("-fx-text-fill: gray;");
        }
    }

    @FXML
    private void handleInvia() {
        statusLabel.setVisible(false);
        String titoloText = titolo.getText().trim();
        String linkText = youtubeLink.getText().trim();
        Genere selectedGenere = menugenre.getValue();

        // Validazioni di base
        if (titoloText.isEmpty() || selectedGenere == null || (path == null && linkText.isEmpty())) {
            showStatus("Compila tutti i campi obbligatori.", "red");
            return;
        }

        // Controllo: se sono stati inseriti sia un file che un link, errore
        if (path != null && !linkText.isEmpty()) {
            showStatus("Inserisci solo un file o un link YouTube, non entrambi.", "red");
            return;
        }

        // Controllo: se è stato inserito un link, validalo
        if (!linkText.isEmpty()) {
            if (!isYouTubeLink(linkText)) {
                showStatus("Il link inserito non è un link di YouTube.", "red");
                return;
            }
            if (!isLinkReachable(linkText)) {
                showStatus("Il link non è raggiungibile.", "red");
                return;
            }
        }

        // Raccogli autori
        List<String> autori = new ArrayList<>();
        for (Node node : vboxContainer.getChildren()) {
            if (node instanceof HBox hbox) {
                for (Node child : hbox.getChildren()) {
                    if (child instanceof TextField tf) {
                        String valore = tf.getText().trim();
                        if (!valore.isEmpty()) {
                            autori.add(valore);
                        }
                    }
                }
            }
        }

        if (autori.isEmpty()) {
            showStatus("Inserisci almeno un autore.", "red");
            return;
        }

        // Crea BranoBean
        BranoBean brano;
        if (anno.getValue() == null) {
            brano = new BranoBean(
                    titoloText,
                    selectedGenere,
                    path,
                    autori.toArray(new String[0])
            );
        } else {
            brano = new BranoBean(
                    titoloText,
                    selectedGenere,
                    path,
                    anno.getValue(),
                    autori.toArray(new String[0])
            );
        }


        try {
            caricaDao.caricaBrano(brano);
            showStatus("Brano caricato con successo!", "green");
        } catch (Exception e) {
            showStatus("Errore durante il caricamento.", "red");
        }
    }

    private boolean isYouTubeLink(String link) {
        return link.matches("^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.*$");
    }

    private boolean isLinkReachable(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode < 400);
        } catch (Exception e) {
            return false;
        }
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        statusLabel.setVisible(true);
    }
}
