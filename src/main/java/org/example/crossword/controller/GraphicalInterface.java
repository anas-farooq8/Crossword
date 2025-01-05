package org.example.crossword.controller;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.crossword.model.Case;
import org.example.crossword.model.Word;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GraphicalInterface extends Application {
    private final Map<String, String> gridFiles = Map.of(
            "Sports", "src/grid.txt",
            "Nature", "src/grid2.txt",
            "History", "src/grid3.txt"
    );

    private final Map<String, String> dataFiles = Map.of(
            "Sports", "src/data.txt",
            "Nature", "src/data2.txt",
            "History", "src/data3.txt"
    );

    private final Map<String, ThemeColors> themeColorSchemes = Map.of(
            "Sports", new ThemeColors("#ff4d4d", "#ffe6e6", "#ffcccc", "#ff8080", "#ff1a1a"),
            "Nature", new ThemeColors("#4CAF50", "#E8F5E9", "#C8E6C9", "#A5D6A7", "#81C784"),
            "History", new ThemeColors("#9c27b0", "#F3E5F5", "#E1BEE7", "#CE93D8", "#BA68C8")
    );

    private static class ThemeColors {
        final String primary;      // Main theme color
        final String background;   // Background color
        final String gridBg;      // Grid background
        final String cellBg;      // Normal cell background
        final String highlight;    // Selected/highlighted cell

        ThemeColors(String primary, String background, String gridBg, String cellBg, String highlight) {
            this.primary = primary;
            this.background = background;
            this.gridBg = gridBg;
            this.cellBg = cellBg;
            this.highlight = highlight;
        }
    }

    private final List<Word> words = new ArrayList<>();
    private final List<Case> gridCells = new ArrayList<>();
    private final Map<Integer, List<Button>> wordCellButtons = new HashMap<>();
    private Word selectedWord = null;
    private Button selectedButton = null;
    private TextField guessInput;
    private Label scoreValue;
    private int totalScore = 0;
    private VBox controlsBox;
    private final double CELL_SIZE = 50;
    private GridPane crosswordGrid;
    private String currentTheme = "Sports";

    private void resetGame() {
        words.clear();
        gridCells.clear();
        wordCellButtons.clear();
        selectedWord = null;
        selectedButton = null;
        totalScore = 0;
        scoreValue.setText("0");
        crosswordGrid.getChildren().clear();
        loadWords(dataFiles.get(currentTheme));
        loadGrid(gridFiles.get(currentTheme));
        populateGridPane(crosswordGrid);
        applyThemeColors(currentTheme);
    }

    private void applyThemeColors(String theme) {
        ThemeColors colors = themeColorSchemes.get(theme);

        BorderPane root = (BorderPane) crosswordGrid.getParent();
        root.setStyle("-fx-background-color: " + colors.background + ";");
        crosswordGrid.setStyle("-fx-background-color: " + colors.gridBg + "; -fx-background-radius: 10;");

        HBox topMenu = (HBox) root.getTop();
        Label title = (Label) topMenu.getChildren().get(1);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 0 0 0 225; -fx-text-fill: " + colors.primary + ";");

        String buttonStyle = """
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14px;
            -fx-min-width: 200px;
        """.formatted(colors.primary);

        VBox gameControlsSection = (VBox) controlsBox.getChildren().get(0);
        gameControlsSection.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .forEach(button -> button.setStyle(buttonStyle));

        VBox scoreSection = (VBox) controlsBox.getChildren().get(2);
        scoreSection.getChildren().stream()
                .filter(node -> node instanceof Label && !(node.equals(scoreValue)))
                .map(node -> (Label) node)
                .forEach(label -> label.setStyle("""
                -fx-background-color: %s;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                -fx-min-width: 200px;
                -fx-alignment: center;
            """.formatted(colors.highlight)));

        scoreValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + colors.primary + "; -fx-alignment: center;");
    }

    private void createGuessInputSection() {
        VBox gameControlsSection = (VBox) controlsBox.getChildren().get(0);
        gameControlsSection.getChildren().removeIf(node -> node instanceof HBox && "guessBox".equals(node.getId()));

        if (selectedWord != null && !selectedWord.isSolved()) {
            HBox guessBox = new HBox(10);
            guessBox.setId("guessBox");
            guessBox.setAlignment(Pos.CENTER);
            guessBox.setPrefWidth(200);

            guessInput = new TextField();
            guessInput.setPromptText("Enter your guess");
            guessInput.setPrefWidth(120);
            guessInput.setStyle("""
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                -fx-font-size: 12px;
            """);

            ThemeColors colors = themeColorSchemes.get(currentTheme);
            Button submitButton = new Button("Submit");
            submitButton.setPrefWidth(70);
            submitButton.setStyle("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                -fx-cursor: hand;
                -fx-font-size: 10px;
            """.formatted(colors.primary));

            submitButton.setOnAction(e -> handleGuess());

            guessBox.getChildren().addAll(guessInput, submitButton);
            gameControlsSection.getChildren().add(guessBox);
        }
    }

    private void handleGuess() {
        if (selectedWord == null || guessInput == null) return;

        String guess = guessInput.getText().toUpperCase();
        String solution = selectedWord.getSolution().toUpperCase();

        if (guess.length() != solution.length()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText("Your guess must be " + solution.length() + " letters long!");
            alert.showAndWait();
            return;
        }

        selectedWord.setCurrentAnswer(guess);
        List<Button> wordButtons = wordCellButtons.get(selectedWord.getWordNumber());

        if (wordButtons != null) {
            for (int i = 0; i < solution.length() && i < wordButtons.size(); i++) {
                Button cellButton = wordButtons.get(i);
                if (guess.charAt(i) == solution.charAt(i)) {
                    cellButton.setText(String.valueOf(guess.charAt(i)));
                } else {
                    cellButton.setText("-");
                }
            }
        }

        if (guess.equalsIgnoreCase(solution)) {
            selectedWord.setSolved(true);
            totalScore += selectedWord.getScore();
            scoreValue.setText(String.valueOf(totalScore));
            createGuessInputSection();

            ThemeColors colors = themeColorSchemes.get(currentTheme);
            if (wordButtons != null) {
                String solvedStyle = """
                    -fx-background-color: %s;
                    -fx-min-width: %fpx;
                    -fx-min-height: %fpx;
                    -fx-max-width: %fpx;
                    -fx-max-height: %fpx;
                """.formatted(colors.cellBg, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);

                wordButtons.forEach(button -> button.setStyle(solvedStyle));
            }

            selectedWord = null;
            selectedButton = null;
        }
    }

    private void loadGrid(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line = br.readLine();
            if (line == null) {
                throw new IOException("The grid file is empty.");
            }

            String[] gridSize = line.split(",");
            if (gridSize.length < 2) {
                throw new IOException("Invalid grid dimensions in the grid file.");
            }

            int rows = Integer.parseInt(gridSize[0].trim());
            int cols = Integer.parseInt(gridSize[1].trim());

            int rowCount = 0;
            while ((line = br.readLine()) != null) {
                rowCount++;
                String[] lineValues = line.split(",");
                if (lineValues.length != cols) {
                    throw new IOException("Invalid number of columns at row " + rowCount);
                }

                for (int j = 0; j < cols; j++) {
                    int cellValue = Integer.parseInt(lineValues[j].trim());
                    Case cell = new Case(rowCount - 1, j);

                    if (cellValue == -1) {
                        cell.setPartOfWord(false, -1);
                    } else if (cellValue == 0) {
                        cell.setPartOfWord(true, -1);
                    } else {
                        cell.setPartOfWord(true, cellValue);
                    }

                    gridCells.add(cell);
                }
            }

            if (rowCount != rows) {
                throw new IOException("Invalid number of rows.");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading grid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateGridPane(GridPane crosswordGrid) {
        Map<Case, Button> allButtons = new HashMap<>();
        ThemeColors colors = themeColorSchemes.get(currentTheme);

        for (Case cell : gridCells) {
            Button cellButton = new Button();
            cellButton.setMinSize(CELL_SIZE, CELL_SIZE);
            cellButton.setMaxSize(CELL_SIZE, CELL_SIZE);

            String baseStyle = """
                -fx-background-radius: 5;
                -fx-font-family: 'Arial';
                -fx-font-weight: bold;
                -fx-font-size: 14px;
            """;

            allButtons.put(cell, cellButton);

            if (!cell.isPartOfWord()) {
                cellButton.setStyle(baseStyle + "-fx-background-color: #e0e0e0;");
            } else if (cell.getWordNumber() > 0) {
                cellButton.setText(String.valueOf(cell.getWordNumber()));
                cellButton.setStyle(baseStyle + "-fx-background-color: " + colors.cellBg + ";");

                int wordIndex = cell.getWordNumber() - 1;
                if (wordIndex < words.size()) {
                    Word word = words.get(wordIndex);
                    word.setWordNumber(cell.getWordNumber());
                    wordCellButtons.put(cell.getWordNumber(), new ArrayList<>());

                    cellButton.setOnMouseClicked(event -> {
                        if (word.isSolved()) return;

                        if (selectedWord != null && selectedWord != word && !selectedWord.isSolved()) {
                            clearWordCells(selectedWord);
                        }

                        if (selectedButton != null) {
                            selectedButton.setStyle(selectedButton.getStyle()
                                    .replace("-fx-background-color: " + colors.highlight + ";",
                                            "-fx-background-color: " + colors.cellBg + ";"));
                        }

                        cellButton.setStyle(baseStyle + "-fx-background-color: " + colors.highlight + ";");
                        selectedWord = word;
                        selectedButton = cellButton;
                        createGuessInputSection();

                        if (event.getClickCount() == 2) {
                            showClueDialog(word);
                        }
                    });
                }
            }

            crosswordGrid.add(cellButton, cell.getCol(), cell.getRow());
        }

        // Second pass for word cells
        for (Word word : words) {
            Case startCell = findStartCell(word.getWordNumber());
            if (startCell != null) {
                List<Case> wordCells = new ArrayList<>();
                if (word.isHorizontal()) {
                    int col = startCell.getCol() + 1;
                    int row = startCell.getRow();
                    for (int i = 0; i < word.getSolution().length(); i++) {
                        Case cell = findCell(row, col + i);
                        if (cell != null) {
                            wordCells.add(cell);
                        }
                    }
                } else {
                    int col = startCell.getCol();
                    int row = startCell.getRow() + 1;
                    for (int i = 0; i < word.getSolution().length(); i++) {
                        Case cell = findCell(row + i, col);
                        if (cell != null) {
                            wordCells.add(cell);
                        }
                    }
                }

                List<Button> buttons = wordCellButtons.computeIfAbsent(word.getWordNumber(), k -> new ArrayList<>());
                for (Case cell : wordCells) {
                    Button button = allButtons.get(cell);
                    if (button != null) {
                        buttons.add(button);
                    }
                }
            }
        }
    }

    private void showClueDialog(Word word) {
        Alert clueDialog = new Alert(Alert.AlertType.INFORMATION);
        clueDialog.setTitle("Clue");
        clueDialog.setHeaderText("Word Clue");
        clueDialog.setContentText(word.getClue());

        DialogPane dialogPane = clueDialog.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #f5f5f5;
            -fx-padding: 20;
        """);
        dialogPane.getStyleClass().add("custom-alert");

        clueDialog.showAndWait();
    }

    private Case findStartCell(int wordNumber) {
        return gridCells.stream()
                .filter(cell -> cell.getWordNumber() == wordNumber)
                .findFirst()
                .orElse(null);
    }

    private Case findCell(int row, int col) {
        return gridCells.stream()
                .filter(cell -> cell.getRow() == row && cell.getCol() == col)
                .findFirst()
                .orElse(null);
    }

    private void clearWordCells(Word word) {
        List<Button> wordButtons = wordCellButtons.get(word.getWordNumber());
        if (wordButtons != null) {
            for (Button btn : wordButtons) {
                btn.setText("");
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Crossword Game");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // Top Menu Section with proper alignment
        HBox topMenu = new HBox(15);
        topMenu.setPadding(new Insets(0, 0, 20, 0));
        topMenu.setPrefWidth(800);

        HBox themeSection = new HBox(10);
        themeSection.setAlignment(Pos.CENTER_LEFT);
        Label themeLabel = new Label("Choose Theme:");
        themeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ComboBox<String> themeDropdown = new ComboBox<>();
        themeDropdown.getItems().addAll("Sports", "Nature", "History");
        themeDropdown.getSelectionModel().selectFirst();
        themeDropdown.setStyle("""
            -fx-background-radius: 5;
            -fx-padding: 5;
            -fx-background-color: #f5f5f5;
        """);

        // add the combobox to the theme section
        themeSection.getChildren().addAll(themeLabel, themeDropdown);

        themeDropdown.setOnAction(event -> {
            String selectedTheme = themeDropdown.getValue();
            if (!selectedTheme.equals(currentTheme)) {
                currentTheme = selectedTheme;
                resetGame();
            }
        });

        Label title = new Label("Crossword Challenge");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 0 0 0 225; -fx-text-fill: #2196F3;");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setAlignment(Pos.CENTER);

        topMenu.getChildren().addAll(themeSection, title);
        root.setTop(topMenu);

        // Crossword Grid Section
        crosswordGrid = new GridPane();
        crosswordGrid.setPadding(new Insets(20));
        crosswordGrid.setHgap(5);
        crosswordGrid.setVgap(5);
        crosswordGrid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        root.setLeft(crosswordGrid);

        // Controls Section
        controlsBox = new VBox(20);
        controlsBox.setPadding(new Insets(20));
        controlsBox.setPrefWidth(250); // Increased width
        controlsBox.setAlignment(Pos.TOP_CENTER);

        // Game Controls Section
        VBox gameControlsSection = new VBox(10);
        gameControlsSection.setAlignment(Pos.CENTER);
        gameControlsSection.setPadding(new Insets(15));
        gameControlsSection.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        // Score Section
        VBox scoreSection = new VBox(10);
        scoreSection.setAlignment(Pos.CENTER);
        scoreSection.setPadding(new Insets(15));
        scoreSection.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        // Style the buttons
        Button lettersButton = new Button("LETTERS");
        Button solutionButton = new Button("SOLUTION");
        Button okButton = new Button("OK");
        Button helpButton = new Button("HELP");

        String buttonStyle = """
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14px;
            -fx-min-width: 200px;
        """;

        String buttonHoverStyle = """
            -fx-background-color: #45a049;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);
        """;

        for (Button btn : Arrays.asList(lettersButton, solutionButton, okButton, helpButton)) {
            btn.setStyle(buttonStyle);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40); // Fixed height for buttons

            btn.setOnMouseEntered(e -> btn.setStyle(buttonStyle + buttonHoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(buttonStyle));
        }

        // Style the score labels
        String scoreLabelStyle = """
            -fx-background-color: #FFC107;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-min-width: 200px;
            -fx-alignment: center;
        """;

        String scoreValueStyle = """
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: #2196F3;
            -fx-alignment: center;
        """;

        Label scoreLabel = new Label("YOUR SCORE");
        scoreValue = new Label("0");
        Label maxScoreLabel = new Label("MAXIMUM");
        Label maxScoreValue = new Label("125");

        scoreLabel.setStyle(scoreLabelStyle);
        maxScoreLabel.setStyle(scoreLabelStyle);
        scoreValue.setStyle(scoreValueStyle);
        maxScoreValue.setStyle(scoreValueStyle);

        // Center align all labels
        scoreLabel.setAlignment(Pos.CENTER);
        scoreValue.setAlignment(Pos.CENTER);
        maxScoreLabel.setAlignment(Pos.CENTER);
        maxScoreValue.setAlignment(Pos.CENTER);

        // Add controls to their respective sections
        gameControlsSection.getChildren().addAll(
                lettersButton,
                solutionButton,
                okButton
        );

        scoreSection.getChildren().addAll(
                scoreLabel,
                scoreValue,
                maxScoreLabel,
                maxScoreValue,
                new Region(),  // Add spacing
                helpButton
        );

        // Add sections to the main controlsBox with a spacer in between
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        controlsBox.getChildren().addAll(
                gameControlsSection,
                spacer,
                scoreSection
        );

        root.setRight(controlsBox);

        // Button Actions
        solutionButton.setOnAction(event -> {
            if (selectedWord != null) {
                Alert solutionDialog = new Alert(Alert.AlertType.INFORMATION);
                solutionDialog.setTitle("Solution");
                solutionDialog.setHeaderText("Solution for the Selected Word");
                solutionDialog.setContentText("The solution is: " + selectedWord.getSolution());

                DialogPane dialogPane = solutionDialog.getDialogPane();
                dialogPane.setStyle("""
                    -fx-background-color: #f5f5f5;
                    -fx-padding: 20;
                """);

                solutionDialog.showAndWait();

                // Reset selection without changing button size
                if (selectedButton != null) {
                    selectedButton.setStyle(selectedButton.getStyle()
                            .replace("-fx-background-color: #FFA726;", "-fx-background-color: #FFF59D;"));
                    selectedButton = null;
                }
                selectedWord = null;

                // Remove guess box
                VBox gameControlsSection1 = (VBox) controlsBox.getChildren().get(0);
                gameControlsSection1.getChildren().removeIf(node ->
                        node instanceof HBox && "guessBox".equals(node.getId()));
            }
        });

        lettersButton.setOnAction(event -> {
            if (selectedWord != null) {
                List<Character> shuffledLetters = new ArrayList<>();
                for (char c : selectedWord.getSolution().toCharArray()) {
                    shuffledLetters.add(c);
                }
                Collections.shuffle(shuffledLetters);

                StringBuilder shuffledString = new StringBuilder();
                for (char c : shuffledLetters) {
                    shuffledString.append(c);
                }

                Alert lettersDialog = new Alert(Alert.AlertType.INFORMATION);
                lettersDialog.setTitle("Shuffled Letters");
                lettersDialog.setHeaderText("Here are the shuffled letters:");
                lettersDialog.setContentText(shuffledString.toString());

                DialogPane dialogPane = lettersDialog.getDialogPane();
                dialogPane.setStyle("""
                    -fx-background-color: #f5f5f5;
                    -fx-padding: 20;
                """);

                lettersDialog.showAndWait();

                // Reset selection without changing button size
                if (selectedButton != null) {
                    selectedButton.setStyle(selectedButton.getStyle()
                            .replace("-fx-background-color: #FFA726;", "-fx-background-color: #FFF59D;"));
                    selectedButton = null;
                }
                selectedWord = null;

                // Remove guess box
                VBox gameControlsSection2 = (VBox) controlsBox.getChildren().get(0);
                gameControlsSection2.getChildren().removeIf(node ->
                        node instanceof HBox && "guessBox".equals(node.getId()));
            }
        });

        helpButton.setOnAction(event -> {
            Alert helpDialog = new Alert(Alert.AlertType.INFORMATION);
            helpDialog.setTitle("Help");
            helpDialog.setHeaderText("Crossword Game Info");
            helpDialog.setContentText("""
                Game Name: Crossword Challenge
                Author: xyz
                
                How to Play:
                1. Click on a numbered cell to select a word
                2. Double-click to see the clue
                3. Use LETTERS button for letter hints
                4. Use SOLUTION button if you're stuck
                
                Score points by correctly guessing words!
                """);

            DialogPane dialogPane = helpDialog.getDialogPane();
            dialogPane.setStyle("""
                -fx-background-color: #f5f5f5;
                -fx-padding: 20;
            """);

            helpDialog.showAndWait();
        });

        loadWords("src/data.txt");
        loadGrid("src/grid.txt");
        applyThemeColors(currentTheme);
        populateGridPane(crosswordGrid);

        Scene scene = new Scene(root, 1100, 950); // Increased width to accommodate larger controls
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadWords(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Reading the Theme
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", 3);
                boolean isHorizontal = parts[0].trim().equalsIgnoreCase("H");
                String clue = parts[1].replace("\"", "").trim();
                String solution = parts[2].trim();
                Word word = new Word(clue, solution, isHorizontal, -1, -1);
                words.add(word);
            }
        } catch (IOException e) {
            System.err.println("Error loading words: " + e.getMessage());
        }
    }
}