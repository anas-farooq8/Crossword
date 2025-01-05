package org.example.crossword;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.crossword.controller.GraphicalInterface;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Create the graphical interface
        GraphicalInterface gui = new GraphicalInterface();
        // Start the graphical interface
        gui.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
