package com.dashapp.controller;

import com.dashapp.model.BranoBean;
import com.dashapp.model.MetaBean;
import com.dashapp.model.NotaBean;
import com.dashapp.model.NotaDao;
import com.dashapp.view.ViewNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class MetaController implements Initializable {
    @FXML private DatePicker dataPicker;
    @FXML private TextField InizioField;
    @FXML private TextField FineField;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    @FXML private TextArea testoLiberoArea;
    private NotaDao notadao = new NotaDao();
    private final NotaDao notaDao = new NotaDao();
    private BranoBean brano;
    @FXML private CheckComboBox<String> strumentiComboBox;
    @FXML private CheckComboBox<String> esecutoriComboBox;
    private Map<Integer, String> mapStrumenti;
    private Map<Integer, String> mapEsecutori;
    private ObservableList<String> strumentiList;
    private ObservableList<String> esecutoriList;

    public void setBrano(BranoBean brano) {
        this.brano = brano;
    }

    @FXML
    private void salvaMeta() {
        MetaBean meta = new MetaBean();

        List<String> strumentiSelezionati = new ArrayList<>(strumentiComboBox.getCheckModel().getCheckedItems());
        strumentiSelezionati.remove("Aggiungi nuovo...");
        List<String> esecutoriSelezionati = new ArrayList<>(esecutoriComboBox.getCheckModel().getCheckedItems());
        esecutoriSelezionati.remove("Aggiungi nuovo...");

        try {
            LocalTime inizio = LocalTime.parse(InizioField.getText(), formatter);
            LocalTime fine = LocalTime.parse(FineField.getText(), formatter);

            Time timeInizio = Time.valueOf(inizio);
            Time timeFine = Time.valueOf(fine);

            meta.setIdBrano(brano.getId());
            meta.setIdUtente(ViewNavigator.getUtenteId());
            meta.setDataRegistrazione(Date.valueOf(dataPicker.getValue()));
            meta.setSegmentoFine(timeFine);
            meta.setSegmentoInizio(timeInizio);
            meta.setCommento(testoLiberoArea.getText());

            int notaId = notaDao.inserisciMeta(meta);
            notaDao.inserisciStrumenti(true, true, notaId, mapStrumenti.entrySet()
                    .stream()
                    .filter(entry -> strumentiSelezionati.contains(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
            notaDao.inserisciStrumenti(false, true, notaId, mapEsecutori.entrySet()
                    .stream()
                    .filter(entry -> esecutoriSelezionati.contains(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
            ((Stage) InizioField.getScene().getWindow()).close();
        } catch (DateTimeParseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Formato orario non valido. Usa hh:mm:ss");
            alert.showAndWait();
        }
    }

    private void mostraDialogAggiuntaStrumenti() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuovo Strumento");
        dialog.setHeaderText("Aggiungi uno Strumento");
        dialog.setContentText("Nuovo Strumento");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nome -> {
            if (!strumentiList.contains(nome)) {
                strumentiList.add(strumentiList.size() - 1, nome); // prima di "Aggiungi nuovo..."
                strumentiComboBox.getItems().setAll(strumentiList);
                strumentiComboBox.getCheckModel().check(nome);
                if (!notaDao.addOthers(true, nome)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Errore nel database: inserimento dello strumento fallito");
                    alert.showAndWait();
                }
            }
        });
    }

    private void mostraDialogAggiuntaEsecutori() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuovo Esecutore");
        dialog.setHeaderText("Aggiungi un Esecutore");
        dialog.setContentText("Nuovo Esecutore");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nome -> {
            if (!esecutoriList.contains(nome)) {
                esecutoriList.add(esecutoriList.size() - 1, nome); // prima di "Aggiungi nuovo..."
                esecutoriComboBox.getItems().setAll(esecutoriList);
                esecutoriComboBox.getCheckModel().check(nome);
                if (!notaDao.addOthers(false, nome)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Errore nel database: inserimento dello esecutore fallito");
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    private void chiudi() {
        ((Stage) InizioField.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapStrumenti = notaDao.getOthers(true);
        mapEsecutori = notaDao.getOthers(false);
        List<String> strumentiSelezionati = new ArrayList<>(mapStrumenti.values());
        List<String> esecutoriSelezionati = new ArrayList<>(mapEsecutori.values());
        strumentiSelezionati.add("Aggiungi nuovo...");
        esecutoriSelezionati.add("Aggiungi nuovo...");
        strumentiList = FXCollections.observableArrayList(strumentiSelezionati);
        esecutoriList = FXCollections.observableArrayList(esecutoriSelezionati);

        strumentiComboBox.getItems().setAll(strumentiList);
        strumentiComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {
            if (strumentiComboBox.getCheckModel().isChecked("Aggiungi nuovo...")) {
                strumentiComboBox.getCheckModel().clearCheck("Aggiungi nuovo...");
                mostraDialogAggiuntaStrumenti();
            }
        });
        esecutoriComboBox.getItems().setAll(esecutoriList);
        esecutoriComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {
            if (esecutoriComboBox.getCheckModel().isChecked("Aggiungi nuovo...")) {
                esecutoriComboBox.getCheckModel().clearCheck("Aggiungi nuovo...");
                mostraDialogAggiuntaEsecutori();
            }
        });

        setTimeFormatter(InizioField);
        setTimeFormatter(FineField);
    }

    private void setTimeFormatter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("^\\d{0,2}:?\\d{0,2}:?\\d{0,2}$") ? change : null;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }
}
