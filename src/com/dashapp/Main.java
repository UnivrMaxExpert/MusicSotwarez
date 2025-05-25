package com.dashapp;

import com.dashapp.view.ViewNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/MainView.fxml"));
            Parent root = loader.load();

            // Crea la scena e aggiunge il CSS se esiste
            Scene scene = new Scene(root);
            //scene.getStylesheets().add(getClass().getResource("/resources/css/login-style.css").toExternalForm());
            ViewNavigator.setStage(stage);
            // Imposta e mostra la finestra principale
            ViewNavigator.changeTitle("Login applicazione");
            stage.setScene(scene);
            stage.setResizable(false); // Opzionale, evita il ridimensionamento
            stage.show();


        } catch (Exception e) {
            e.printStackTrace(); // Per debugging
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
