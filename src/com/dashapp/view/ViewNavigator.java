package com.dashapp.view;

import com.dashapp.Main;
import com.dashapp.controller.MainController;
import com.dashapp.model.BranoBean;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;

/**
 * Questa classe permette di salvare variabili utili nella navigazione(utente, brano scelto per essere visualizzato, ...), tipo variabili di sessione
 * Poi permette anche lo spostamento tra le varie view
 * Tiene traccia anche di dove siamo, ponendo allo spostamento da view a view un identificativo univoco
 */
public class ViewNavigator {
    // Reference to the main controller
    private static MainController mainController;
    private static Stage mainstage;
    private static BranoBean brano;
    private static int id;
    private static String link;
    private static MediaPlayer mediaPlayer;
    private static WebEngine webEngine;
    // Current authenticated username
    private static String authenticatedUser = null;
    public static BranoBean getBrano(){return brano;}
    public static void setBrano(BranoBean branox){brano=branox;}
    /**
     * Set the main controller reference
     * @param controller The MainController instance
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
    }
    public static void setStage(Stage stage) {mainstage = stage;}
    /**
     * Load and switch to a view
     *
     */

    public static void changeTitle(String title) {mainstage.setTitle(title);}
    public static int getState(){return mainController.getState();}
    private static void loadView(String fxml) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (webEngine != null)
            webEngine.load(null);
        try {
            URL fxmlUrl = Main.class.getResource("/resources/fxml/" + fxml);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();
            mainController.setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxml);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getRisorsa(Class<T> type) {
        Object risorsa = mediaPlayer == null ? webEngine : mediaPlayer;
        if (type.isInstance(risorsa)) {
            return type.cast(risorsa);
        }
        throw new IllegalStateException("La risorsa non è del tipo richiesto: " + type.getSimpleName());
    }

    public static <T> void setRisorsa(T risorsa) {
        if (risorsa instanceof MediaPlayer) {
            mediaPlayer = (MediaPlayer) risorsa;
        } else if (risorsa instanceof WebEngine) {
            webEngine = (WebEngine) risorsa;
        }
    }

    /**
     * Navigate to the home view
     */
    public static void navigateToHome() {
        mainController.setState(2);
        loadView("home.fxml");
    }
    /**
     * Navigate to the login view
     */
    public static void navigateToLogin() {
        mainController.setState(0);
        loadView("login.fxml");
    }

    public static void navigateToBrano() {
        mainController.setState(5);
        loadView("visualizza.fxml");
    }
    /**
     * Navigate to the register view
     */
    public static void navigateToRegister() {
        mainController.setState(1);
        loadView("registration.fxml");
    }

    /**
     * Navigate to the dashboard view (protected)
     * Will redirect to login if not authenticated
     */
    public static void navigateToCarica() {
        if (isAuthenticated()) {
            mainController.setState(3);
            loadView("carica.fxml");
        } else {
            navigateToLogin();
        }
    }
    public static void navigateToNota() {loadView("nota.fxml");}
    /**
     * Navigate to the profile view (protected)
     * Will redirect to login if not authenticated
     */
    public static void navigateToCatalogo() {
        if (isAuthenticated()) {
            mainController.setState(4);
            loadView("catalogo.fxml");
        } else {
            navigateToLogin();
        }
    }

    public static void navigateToWebView() {
        if (isAuthenticated())
        {
            mainController.setState(5);
            loadView("webview.fxml");
        }
        else
            navigateToLogin();
    }

    public static String getLink() {
        return link;
    }

    public static void setLink(String link) {
        ViewNavigator.link = link;
    }

    public static void navigateToExit() {
        loadView("prova.fxml");
    }

    /**
     * Set the authenticated user
     * @param username The username of the authenticated user
     */
    public static void setAuthenticatedUser(String username) {
        authenticatedUser = username;
    }

    /**
     * Get the authenticated user
     * @return The username of the authenticated user, or null if not authenticated
     */
    public static String getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * Check if a user is authenticated
     * @return true if a user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return authenticatedUser != null;
    }

    /**
     * Logout the current user
     */
    public static void logout() {
        authenticatedUser = null;
        //mainController.updateNavBar(false);
        navigateToHome();
    }

    public static int getUtenteId() {
        return id;
    }

    public static void setUtenteId(int id2) {
         id = id2;
    }

    public static void navigateToRequest() {
        loadView("request.fxml");
    }
}
