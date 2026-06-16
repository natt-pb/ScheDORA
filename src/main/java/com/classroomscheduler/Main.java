package com.classroomscheduler;

import com.classroomscheduler.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Main extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        primaryStage.setTitle("Schedora — Book Smart. Learn Better.");
        
        // Set the application window icon
        try {
            InputStream iconStream = Main.class.getResourceAsStream("/images/app_icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
                System.out.println("Main: Application icon set successfully.");
            } else {
                System.err.println("Main: Could not find app icon at /images/app_icon.png");
            }
        } catch (Exception e) {
            System.err.println("Main: Failed to set app icon: " + e.getMessage());
        }
        
        // Initialize local SQLite database tables and seed defaults
        DatabaseInitializer.initialize();
        
        // Show splash screen on startup (transitions to login automatically)
        setRoot("splash.fxml");
        
        primaryStage.show();
    }

    /**
     * Swaps the primary Scene's root layout.
     * @param fxmlFile the name of the FXML file under /fxml/
     */
    public static void setRoot(String fxmlFile) {
        try {
            System.out.println("Main: Swapping stage root layout to: " + fxmlFile);
            URL url = Main.class.getResource("/fxml/" + fxmlFile);
            if (url == null) {
                System.err.println("Main: Cannot find FXML file: /fxml/" + fxmlFile);
                return;
            }
            Parent root = FXMLLoader.load(url);
            if (stage.getScene() == null) {
                Scene scene = new Scene(root, 1280, 800);
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            System.err.println("Main: Failed to set stage root to " + fxmlFile);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
