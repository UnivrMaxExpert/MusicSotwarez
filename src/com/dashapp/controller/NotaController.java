package com.dashapp.controller;

import com.dashapp.model.BranoBean;
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
    /*Fa le stesse cose che fa più o meno il MetaController*/
public class NotaController implements Initializable {
    @FXML private TextField durataField;
    @FXML private TextField luogoField;
    @FXML private DatePicker dataPicker;
    @FXML private TextField segmentoInizioField;
    @FXML private TextField segmentoFineField;
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
    private void salvaNota() {
        NotaBean nota = new NotaBean();

        List<String> strumentiSelezionati = new ArrayList<>(strumentiComboBox.getCheckModel().getCheckedItems());
        strumentiSelezionati.remove("Aggiungi nuovo...");
        List<String> esecutoriSelezionati = new ArrayList<>(esecutoriComboBox.getCheckModel().getCheckedItems());
        esecutoriSelezionati.remove("Aggiungi nuovo...");

        try {
            LocalTime inizio = LocalTime.parse(segmentoInizioField.getText(), formatter);
            LocalTime fine = LocalTime.parse(segmentoFineField.getText(), formatter);
            LocalTime durata = LocalTime.parse(durataField.getText(), formatter);

            Time timeInizio = Time.valueOf(inizio);
            Time timeFine = Time.valueOf(fine);
            Time timeDurata = Time.valueOf(durata);

            nota.setIdBrano(brano.getId());
            nota.setIdUtente(ViewNavigator.getUtenteId());
            nota.setDurata(timeDurata);
            nota.setLuogoRegistrazione(luogoField.getText());
            nota.setDataRegistrazione(Date.valueOf(dataPicker.getValue()));
            nota.setSegmentoFine(timeFine);
            nota.setSegmentoInizio(timeInizio);
            nota.setTestoLibero(testoLiberoArea.getText());

            int notaId = notaDao.inserisciNota(nota);
            notaDao.inserisciStrumenti(true, false,notaId, mapStrumenti.entrySet()
                    .stream()
                    .filter(entry -> strumentiSelezionati.contains(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
            notaDao.inserisciStrumenti(false, false,notaId, mapEsecutori.entrySet()
                    .stream()
                    .filter(entry -> esecutoriSelezionati.contains(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
            ((Stage) durataField.getScene().getWindow()).close();
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
                if(!notaDao.addOthers(true, nome))
                {
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
                if(!notaDao.addOthers(false, nome))
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Errore nel database: inserimento dello esecutore fallito");
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    private void chiudi() {
        ((Stage) durataField.getScene().getWindow()).close();
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
        //Al posto di un array statico mettere col NotaDao, un query al db sulla tabella degli strumenti
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

        setTimeFormatter(segmentoInizioField);
        setTimeFormatter(segmentoFineField);
        setTimeFormatter(durataField);
    }

    private void setTimeFormatter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("^\\d{0,2}:?\\d{0,2}:?\\d{0,2}$") ? change : null;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }

}
