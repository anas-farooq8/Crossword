module org.example.crossword {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.crossword to javafx.fxml;
    exports org.example.crossword;
    exports org.example.crossword.model;
    opens org.example.crossword.model to javafx.fxml;
    exports org.example.crossword.controller;
    opens org.example.crossword.controller to javafx.fxml;
}