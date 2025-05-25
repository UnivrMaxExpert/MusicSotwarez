package com.dashapp.view;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class to apply predefined animations to any JavaFX Node.
 */
public class AnimationUtil {
    private static FadeTransition ft;
    private static TranslateTransition tt;
    // Fade In
    public static void fadeIn(Node node, double durationMillis) {
        ft = new FadeTransition(Duration.millis(durationMillis), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    // Fade Out
    public static void fadeOut(Node node, double durationMillis) {
        ft = new FadeTransition(Duration.millis(durationMillis), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.play();
    }

    // Slide in from left
    public static void slideInFromLeft(Node node, double durationMillis, double distance) {
        node.setTranslateX(-distance);
        tt = new TranslateTransition(Duration.millis(durationMillis), node);
        tt.setToX(0);
        tt.play();
    }

    // Slide in from right
    public static void slideInFromRight(Node node, double durationMillis, double distance) {
        node.setTranslateX(distance);
        tt = new TranslateTransition(Duration.millis(durationMillis), node);
        tt.setToX(0);
        tt.play();
    }

    // Slide in from top
    public static void slideInFromTop(Node node, double durationMillis, double distance) {
        node.setTranslateY(-distance);
        tt = new TranslateTransition(Duration.millis(durationMillis), node);
        tt.setToY(0);
        tt.play();
    }

    // Slide in from bottom
    public static void slideInFromBottom(Node node, double durationMillis, double distance) {
        node.setTranslateY(distance);
        tt = new TranslateTransition(Duration.millis(durationMillis), node);
        tt.setToY(0);
        tt.play();
    }

    // Scale in (zoom)
    public static void scaleIn(Node node, double durationMillis) {
        node.setScaleX(0.0);
        node.setScaleY(0.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(durationMillis), node);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    // Pulse (scaling loop effect)
    public static void pulse(Node node, double durationMillis) {
        ScaleTransition st = new ScaleTransition(Duration.millis(durationMillis), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.1);
        st.setToY(1.1);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();
    }

    // Rotate continuously
    public static void rotate(Node node, double durationMillis, boolean infinite) {
        RotateTransition rt = new RotateTransition(Duration.millis(durationMillis), node);
        rt.setByAngle(360);
        rt.setCycleCount(infinite ? Animation.INDEFINITE : 1);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();
    }

    // Shake effect
    public static void shake(Node node, double durationMillis) {
        tt = new TranslateTransition(Duration.millis(durationMillis), node);
        tt.setFromX(-10);
        tt.setByX(20);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    // Bounce effect
    public static void bounce(Node node, double durationMillis) {
        tt = new TranslateTransition(Duration.millis(durationMillis), node);
        tt.setFromY(0);
        tt.setByY(-20);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

}

