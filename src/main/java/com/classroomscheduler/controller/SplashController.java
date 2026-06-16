package com.classroomscheduler.controller;

import com.classroomscheduler.Main;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private ImageView splashImage;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Animate the splash screen
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), splashImage);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Transition to login after 2.5 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(event -> {
            lblStatus.setText("Ready!");
            progressIndicator.setProgress(1.0);

            // Brief pause then switch to login
            PauseTransition switchPause = new PauseTransition(Duration.millis(400));
            switchPause.setOnFinished(e -> Main.setRoot("login.fxml"));
            switchPause.play();
        });
        pause.play();
    }
}
