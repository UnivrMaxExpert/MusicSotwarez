package com.dashapp.view;

import com.dashapp.Main;
import com.dashapp.controller.MainController;
import com.dashapp.model.BranoBean;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;

/**
 * This class handles navigation between different views in the application.
 * It works as a bridge between controllers and views, allowing for simplified navigation.
 */
public class ViewNavigator {
    // Reference to the main controller
    private static MainController mainController;
    private static Stage mainstage;
    private static BranoBean brano;
    private static int id;
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
}
