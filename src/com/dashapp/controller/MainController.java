package com.dashapp.controller;

import com.dashapp.view.ViewNavigator;
import com.dashapp.view.ViewState;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML
    private BorderPane mainContainer;

    private ViewState currentState;

    @FXML
    private VBox navBarContainer;

    //private NavBar navBar;

    @FXML
    public void initialize() {
        // Set up the navigation bar
        /*navBar = new NavBar();
        navBarContainer.getChildren().add(navBar);*/
        currentState = ViewState.LOGIN;
        // Register this controller with the ViewNavigator
        ViewNavigator.setMainController(this);

        // Load the home view by default
        ViewNavigator.navigateToLogin();
    }

    /**
     * Set the content of the main area
     */
    public void setContent(Node content) {
        mainContainer.setCenter(content);
    }
    public int getState() {return currentState.ordinal();}
    public void setState(int index) {
        for (ViewState state : ViewState.values()) {
            if (state.index == index) {
                this.currentState = state;
                return;
            }
        }
        throw new IllegalArgumentException("Indice di stato non valido: " + index);
    }
}
