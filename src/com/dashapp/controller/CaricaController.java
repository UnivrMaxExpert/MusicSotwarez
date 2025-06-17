package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.CaricaDao;
import com.dashapp.model.Genere;
import com.dashapp.model.Ruoli;
import com.dashapp.view.ViewNavigator;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import com.jfoenix.controls.JFXTextField;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class CaricaController implements Initializable {
    /*Gestisce gli input dall'interfaccia grafica e ne prende i dati per passarli al CaricaDao per caricare i brani*/
    @FXML private Button btn;
    @FXML private ComboBox<Genere> menugenre;
    @FXML private ComboBox<Ruoli> ruoloCombo;
    @FXML private TextField titolo;
    @FXML private Button add, add2;
    @FXML private VBox vboxContainer, autoriContainer, strumentiContainer, ruoloContainer;
    @FXML private CheckBox isConcerto, isInter;
    @FXML private TextField anno;
    @FXML private Label fileLabel;
    @FXML private JFXTextField youtubeLink;

    private String path;
    private final CaricaDao caricaDao = new CaricaDao();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Popola ComboBox con enum Genere
        ObservableList<Genere> generiList = FXCollections.observableArrayList(Genere.values());
        menugenre.setItems(generiList);
        menugenre.setValue(Genere.HIP_HOP); // valore di default
        ruoloCombo.getItems().setAll(Ruoli.values());
        // Spinner per anni

        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();

            // Permetti campo vuoto
            if (newText.isEmpty())
                return change;
            // Consenti solo cifre
            if (!newText.matches("\\d*"))
                return null;

            // Limita a 4 cifre
            if (newText.length() > 4)
                return null;

            // Verifica intervallo 1900-2025 se ha 4 cifre
            if(newText.length()==4)
            {
                try {
                    int num = Integer.parseInt(newText);
                    if (num >= 1900 && num <= 2025)
                        return change;
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Errore! Anno deve essere compreso tra 1900-2025");
                        alert.showAndWait();
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return change;
        });
        anno.setTextFormatter(formatter);
        anno.setTooltip(new Tooltip("Inserisci solo numeri, oppure lascia vuoto se sconosciuto"));

        isInter.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // newValue è TRUE se la CheckBox è selezionata, FALSE se deselezionata
            strumentiContainer.setVisible(newValue);
            ruoloContainer.setVisible(newValue);
            strumentiContainer.setManaged(newValue);
            ruoloContainer.setManaged(newValue);
        });
        // Label iniziale
        fileLabel.setText("Nessun file selezionato");
        fileLabel.setStyle("-fx-text-fill: gray;");
    }

    @FXML
    private void handleAdd() {
        HBox currentBox = (HBox) add.getParent();
        currentBox.getChildren().remove(add);

        HBox newHBox = new HBox(15);
        newHBox.setAlignment(Pos.CENTER_LEFT);
        newHBox.setSpacing(10);

        TextField newTextField = new TextField();
        newTextField.setPrefWidth(381);
        newTextField.setPromptText("Inserisci autore");

        newHBox.getChildren().addAll(newTextField, add);
        autoriContainer.getChildren().add(newHBox);

        newHBox.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), newHBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    private void handleAdd2() {
        HBox currentBox = (HBox) add2.getParent();
        currentBox.getChildren().remove(add2);

        HBox newHBox = new HBox(15);
        newHBox.setAlignment(Pos.CENTER_LEFT);
        newHBox.setSpacing(10);

        TextField newTextField = new TextField();
        newTextField.setPrefWidth(381);
        newTextField.setPromptText("Inserisci strumento");

        newHBox.getChildren().addAll(newTextField, add2);
        strumentiContainer.getChildren().add(newHBox);

        newHBox.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), newHBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    public void openFileChooser() {
        Stage stage = (Stage) btn.getScene().getWindow();
        path = caricaDao.openFileChooser(stage);

        if (path != null) {
            fileLabel.setText(new File(path).getName());
            fileLabel.setStyle("-fx-text-fill: green;");
        } else {
            fileLabel.setText("Nessun file selezionato");
            fileLabel.setStyle("-fx-text-fill: gray;");
        }
    }

    @FXML
    private void handleInvia() throws IOException {
        String titoloText = titolo.getText().trim();
        String linkText = youtubeLink.getText().trim();
        Genere selectedGenere = menugenre.getValue();
        if (path != null)
            caricaDao.copiaBrano(path);

        if (titoloText.isEmpty() || selectedGenere == null || (path == null && linkText.isEmpty())) {
            showStatus("Compila tutti i campi obbligatori.");
            return;
        }

        if (path != null && !linkText.isEmpty()) {
            showStatus("Inserisci solo un file o un link YouTube, non entrambi.");
            return;
        }

        if (isInter.isSelected()) {
            // Verifica se il ruolo è stato selezionato
            if (ruoloCombo.getValue() == null) {
                showStatus("Seleziona un ruolo.");
                return;
            }

            // Controlla tutti i TextField nello strumentiContainer
            boolean strumentoVuoto = false;
            for (Node node : strumentiContainer.getChildren()) {
                if (node instanceof HBox hbox) {
                    for (Node child : hbox.getChildren()) {
                        if (child instanceof TextField tf) {
                            String valore = tf.getText().trim();
                            if (valore.isEmpty()) {
                                strumentoVuoto = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (strumentoVuoto) {
                showStatus("Inserisci tutti gli strumenti oppure rimuovi i campi vuoti.");
                return;
            }
        }

        if (!linkText.isEmpty()) {
            if (!isYouTubeLink(linkText)) {
                showStatus("Il link inserito non è un link di YouTube.");
                return;
            }
            if (!isLinkReachable(linkText)) {
                showStatus("Il link non è raggiungibile.");
                return;
            }
        }

        List<String> autori = new ArrayList<>();
        for (Node node : autoriContainer.getChildren()) {
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

        List<String> strumenti = null;
        if (isInter.isSelected()) {
            strumenti = new ArrayList<>();
            for (Node node : strumentiContainer.getChildren()) {
                if (node instanceof HBox hbox) {
                    for (Node child : hbox.getChildren()) {
                        if (child instanceof TextField tf) {
                            String valore = tf.getText().trim();
                            if (!valore.isEmpty())
                                strumenti.add(valore);
                        }
                    }
                }
            }
        }

        if (autori.isEmpty()) {
            showStatus("Inserisci almeno un autore.");
            return;
        }

        BranoBean brano;
        brano = new BranoBean(titoloText, ViewNavigator.getUtenteId(),
                selectedGenere, (path != null) ? path : linkText,
                anno.getText().isEmpty() ? null : Integer.valueOf(anno.getText())
                , isConcerto.isSelected(), autori.toArray(new String[0]));
        brano.setId(caricaDao.caricaBrano(brano));
        if(isInter.isSelected())
            brano.setRuolo(caricaDao.autoInterpretato(brano, String.valueOf(ruoloCombo.getValue())));
        boolean caricamento = caricaDao.caricaStrumentiPerBrano(brano.getId(), strumenti);
        boolean caricamento2 = caricaDao.caricaArtistiPerBrano(brano.getId(), Arrays.stream(brano.getAutori().split(",")).toList());
        if (caricamento && caricamento2) {
            showSuccessDialog();
        } else {
            showErrorAlert();
        }
    }

    private void showSuccessDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Caricamento completato");

        // Recupera la finestra principale (opzionale)
        Window owner = dialog.getDialogPane().getScene() != null ?
                dialog.getDialogPane().getScene().getWindow() :
                null;

        // Carica il CSS personalizzato
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/resources/css/carica-style.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        // Messaggio
        Label label = new Label("Il brano è stato caricato correttamente!");
        label.getStyleClass().add("label");

        // Bottoni
        ButtonType homeButton = new ButtonType("Torna alla Home", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(homeButton, ButtonType.CANCEL);

        // Imposta contenuto
        dialog.getDialogPane().setContent(label);

        // Mostra la finestra
        Optional<ButtonType> result = dialog.showAndWait();
        result.ifPresent(response -> {
            if (response == homeButton) {
                ViewNavigator.navigateToHome();
            }
        });
    }

    /**
     * Mostra un alert di errore.
     */
    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore di caricamento");
        alert.setHeaderText("Si è verificato un errore durante il caricamento del brano.");
        alert.setContentText("Controlla il file selezionato e riprova.");
        alert.showAndWait();
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

    private void showStatus(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Errore! "+message);
        alert.showAndWait();
    }
}


