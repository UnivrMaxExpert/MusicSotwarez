package com.dashapp.controller;

import com.dashapp.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebViewController implements Initializable {

    @FXML
    private WebView webview;
    @FXML
    private SectionController section;
    // Pattern per estrarre l'ID del video da vari formati di URL YouTube
    private final Pattern pattern = Pattern.compile(
            "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine webEngine = webview.getEngine();
        String link = ViewNavigator.getLink();
        System.out.println("Link ricevuto: " + link);

        if (link != null && !link.trim().isEmpty()) {
            link = link.trim();

            Matcher matcher = pattern.matcher(link);
            if (matcher.find()) {
                String videoId = matcher.group(0);
                String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;

                // Carica il link classico (l'interfaccia YouTube sarà completa)
                webEngine.load(youtubeUrl);
                ViewNavigator.setRisorsa(webEngine);
            } else {
                // Se non è un link YouTube, caricalo normalmente
                if (!link.startsWith("https://")) {
                    link = "https://" + link;
                }
                try {
                    URL url = new URL(link);
                    webEngine.load(url.toExternalForm());
                    System.out.println("Caricato URL generico: " + url.toExternalForm());
                } catch (MalformedURLException e) {
                    System.err.println("URL malformato: " + link);
                    webEngine.load("https://www.google.com");
                }
            }
        } else {
            System.err.println("Nessun link da caricare.");
            webEngine.load("https://www.google.com");
        }
    }
}
