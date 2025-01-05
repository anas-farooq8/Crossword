package org.example.crossword.model;

public class Word {
    private final String clue;          // The clue for the word
    private final String solution;      // The correct answer
    private final boolean isHorizontal; // Is the word horizontal
    private final int row;              // The row of the first letter of the word
    private final int col;              // The column of the first letter of the word
    private String currentAnswer;       // The current answer
    private boolean solved;             // Is the word solved
    private int wordNumber;             // The number of the word

    // Constructor
    public Word(String clue, String solution, boolean isHorizontal, int row, int col) {
        this.clue = clue;
        this.solution = solution;
        this.isHorizontal = isHorizontal;
        this.row = row;
        this.col = col;
        this.currentAnswer = "";
        this.solved = false;
    }

    // Getters and Setters
    public void setWordNumber(int wordNumber) {
        this.wordNumber = wordNumber;
    }

    public int getWordNumber() {
        return wordNumber;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public boolean isSolved() {
        return solved;
    }

    public String getClue() {
        return clue;
    }

    public String getSolution() {
        return solution;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setCurrentAnswer(String currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    // Returns the score of the word
    public int getScore() {
        if (isSolved()) {
            return solution.length() * 2; // Score if solved without hints
        }
        return 0;
    }

    // Returns the current answer with the correct number of spaces
    public String getCurrentAnswer() {
        return currentAnswer;
    }
}

