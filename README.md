
# Crossword Game Project

## Overview

The Crossword Game application is designed to offer an engaging and interactive crossword puzzle experience with multiple themes. The application includes features such as theme selection, word clues, and scoring based on player performance.

## Features

### Game Mechanics
- Click on a number to display the clue for the word.
- Use the **Solution** button to reveal the current word.
- Use the **Letters** button to display scrambled letters of the word.
- Enter your answer in the corresponding grid and validate with the **OK** button or the Enter key.
- Incorrect answers reveal the correct solution.
- Characters can be deleted using the backspace key, except for validated word characters.
- Switching to a new word without validating resets the previous word's entered letters.
- Previously scrambled letters are remembered for point calculation and will be displayed consistently in future attempts.

### Interface Features
- **Theme Selection**: A dropdown menu to choose from multiple crossword themes.
- **Help Menu**: Includes an **About** section with author information and game rules.
- **Keyboard Shortcuts**: At least one shortcut for menu navigation.
- **GridPane Panel**: Displays the crossword grid, buttons for actions (LETTERS, SOLUTION, OK), score display, and HELP.

### Scoring
- An exact word obtained only using the clue earns twice the word's length in points.
- Using scrambled letters earns points equal to the word's length.
- The maximum points for the entire grid are displayed throughout the game.

### Compound Words
- Hyphens count as one letter.
- Spaces between words are replaced with an asterisk (`*`), e.g., `Jose*lovable`.
- Solutions are written without accent marks or apostrophes.

### Data Files
- **data.txt**: Contains word details for each theme. Format:
  ```
  H,"A hockey game normally lasts_____ minutes",sixty
  ```
  - `H` or `V` for horizontal or vertical orientation.
  - The clue for the word.
  - The word solution.
- **grid.txt**: Defines the grid's structure. Format:
  ```
  15,13
  -1,1,2,0,0,0,0,0,0,1,1,1,1
  ```
  - First line specifies grid size (rows, columns).
  - Subsequent lines define grid cells (-1 for empty, 1 for word numbers, 0 for letter cells).

### Themes
- The project includes at least three themes, with provided data files for each.

### Visual Indicators
- Different colors for:
  - Words found without help.
  - Words found using the **LETTERS** button.
  - Words revealed using the **SOLUTION** button.

### Demo

https://github.com/user-attachments/assets/ec6c700d-c3fc-4f36-b0bb-84c92d96e9fd

