package org.example.crossword.model;

public class Case {
    private final int row;            // row of the case
    private final int col;            // column of the case
    private char currentChar;   // current character of the case
    private boolean isPartOfWord;   // is the case part of a word
    private int wordNumber;         // number of the word the case is part of

    // Constructor
    public Case(int row, int col) {
        this.row = row;
        this.col = col;
        this.currentChar = ' ';
        this.isPartOfWord = false;
        this.wordNumber = -1;
    }

    // Getters and Setters
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public char getCurrentChar() {
        return currentChar;
    }

    public void setCurrentChar(char currentChar) {
        this.currentChar = currentChar;
    }

    public boolean isPartOfWord() {
        return isPartOfWord;
    }

    public void setPartOfWord(boolean partOfWord, int wordNumber) {
        this.isPartOfWord = partOfWord;
        this.wordNumber = wordNumber;
    }

    public int getWordNumber() {
        return wordNumber;
    }
}
