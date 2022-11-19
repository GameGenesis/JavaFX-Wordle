import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AppController {

    // The VBox that contains the rows of letter box labels
    @FXML
    private VBox letterBox;

    // A list of HBoxes that are children of the letterBox VBox parent node
    private List<Node> letterBoxChildren;
    // The HBox that is currently being accessed
    private HBox currentRow;
    // The current row that is receiving user input
    private int currentRowIndex = 0;

    // The VBox that contains the rows of keyboard buttons
    @FXML
    private VBox keyBox;

    // A list of button nodes (keyboard buttons) that are contained within the HBoxes in keyBox
    private List<Node> keyBoxChildren;
    // A list of the revealed letters that are in the correct location in the word
    private List<String> correctLetters;
    // A list of the revealed letters that are in the incorrect location in the word
    private List<String> misplacedLetters;
    // A list of the revealed letters that are not in the word
    private List<String> wrongLetters;
    // a Hashmap containing the number of occurances of each letter in the correct word
    private Map<String, Integer> letterMap;

    // The index of the label in the current row to update the color of
    private int currentletterIndex = 0;
    // The word that was submitted as an attempt (The word that the user typed on the current row)
    private String currentWord = "";

    private List<Object> wordList;
    // The correct word
    private String correctWord = "Rainy";

    // Specifies whether the win animation should be played and whether user input should be disabled (if the player has won)
    private boolean hasWon = false;

    // The VBox that is contained in the StackPane that holds the list of popup messages
    @FXML
    private VBox popupList;

    // The transparent background pane that appears below the help pane
    @FXML
    private Pane helpPaneBackground;

    // The help pane box that contains the game instructions (tutorial)
    @FXML
    private VBox helpPane;

    @FXML
    private void initialize() {
        // Gets a random word from the WordList json file
        getWord();

        // Gets the children of the parent VBox that contains all the letter box rows
        letterBoxChildren = letterBox.getChildren();

        // Gets all the keyboard keys and stores them in a list
        keyBoxChildren = new ArrayList<>();
        List<Node> hboxChildren = keyBox.getChildren();
        for (Node child : hboxChildren) {
            HBox hbox = (HBox)child;
            keyBoxChildren.addAll(hbox.getChildren());
        }

        // Initialize the lists of letters
        correctLetters = new ArrayList<>();
        misplacedLetters = new ArrayList<>();
        wrongLetters = new ArrayList<>();
    }

    /**
     * Called when an onscreen keyboard button is pressed.
     * Gets the event source button and passes in its text value to the getInput method.
     * 
     * @param event The ActionEvent that contains information about the type of event and the event source
     */
    @FXML
    private void keyPressed(ActionEvent event) {
        Object node = event.getSource();
        Button button = (Button)node;
        getInput(button.getText());
    }

    /**
     * Called when the help button (ImageView) is clicked.
     * Fades in the help pane background and the help pane.
     * Translates the help pane upwards.
     * 
     * @param event The MouseEvent that contains information about the mouse pointer
     */
    @FXML
    private void showHelpPane(MouseEvent event) {
        animateHelpPane(false);
    }

    /**
     * Called when the help pane close button (ImageView) is clicked.
     * Animates the help pane in reverse - Fades out the help pane background and the help pane.
     * Translates the help pane downwards.
     * 
     * @param event The MouseEvent that contains information about the mouse pointer
     */
    @FXML
    private void hideHelpPane(MouseEvent event) {
        animateHelpPane(true);
    }

    /**
     * Tries to read the WordList.json file as a JSONObject.
     * Then, gets a JSONArray with the name "wordList" from the object.
     * Gets a random element from the JSONArray and assigns that to correctWord.
     * Converts the JSONAray to a List and assigns that list to wordList
     * 
     * If there is an exception with reading the json file, assigns correctWord to a specificed word
     */
    private void getWord() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(getClass().getResource("WordList.json").toURI())));
            JSONObject obj = new JSONObject(content);
            JSONArray wordArray = obj.getJSONArray("wordList");

            Random random = new Random();
            correctWord = wordArray.getString(random.nextInt(wordArray.length()));

            wordList = wordArray.toList();
        } catch (Exception e) {
            correctWord = "Rainy";
            e.printStackTrace();
        }

    }

    /**
     * Decides what to do based on the key press.
     * If the key is Back Space, remove the last-typed letter (clear the last non-empty label).
     * If the key is Enter, check that all the labels are filled and the letters compose a word that is contained in the word list.
     * If that is the case, animate the letter boxes and indicate through styling what letters are the correct or incorrect locations,
     * and which letters are not in the correct word.
     * If the key is a letter, fill the first empty label in the row with that letter.
     * 
     * @param key The string that corresponds to the key that has been pressed
     */
    public void getInput(String key) {
        // Checks if the player still hasn't won, the list of HBoxes exists, and that the current row is not greater than the last index of the list
        if (letterBoxChildren == null || currentRowIndex > letterBoxChildren.size() - 1 || hasWon) {
            return;
        }

        // Assigns the current row depending on the currentRowIndex
        currentRow = (HBox)letterBoxChildren.get(currentRowIndex);

        // Check if the current key is BACK_SPACE
        if (key.equals("BACK_SPACE")) {
            // Gets the list of children of the current row HBox
            List<Node> children = currentRow.getChildren();

            // Loops over the list of children in reverse (to get the last non-empty label)
            for (int i = children.size() - 1; i >= 0; i--) {
                // Explicit type conversion from Node to Label
                Label label = (Label)children.get(i);

                // Check if the label text is not empty
                if (!label.getText().isEmpty()) {
                    // Remove the label text, update the CSS ID, and change the text color to gray
                    label.setText("");
                    label.setId("letter-box-empty");
                    label.setTextFill(Color.web("#383838"));
                    // Animate the letter box to betetr indicate that the letter has been cleared
                    animateScalingPingPong(label, 0.07, 0.9, 0.9);

                    // Break so only the last letter is cleared
                    break;
                }
            }
            return;
        }

        // Check if the current key is ENTER
        if (key.equals("ENTER")) {
            // Clears the previous current word
            currentWord = "";

            // Creates a new hashmap with the letter string as the key and a double representing the number of times it appears in the correct word as a value
            letterMap = new HashMap<>();

            // Loops over all the letters in the correct word and increments the letter map's corresponding value by 1 for each time that specific letter occurs in the correct word.
            // The merge method is used with Integer::sum to ensure that if the key doesn't exist, the default value will be set to 1. Otherwise, it will add 1 to the existing value.
            for (int i = 0; i < correctWord.length(); i++) {
                letterMap.merge(correctWord.substring(i, i+1).toUpperCase(), 1, Integer::sum);
            }

            // Gets the list of children of the current row HBox
            List<Node> children = currentRow.getChildren();

            // Loops over the list of HBox children
            for (int i = 0; i < children.size(); i++) {
                // Explicit type conversion from Node to Label
                Label label = (Label)children.get(i);
                // Gets the label text from the current label
                String labelText = label.getText();

                // Checks if any of the labels are empty
                if (labelText.isEmpty()) {
                    // Create a popup box saying that there aren't enough letters
                    createDialog("Not enough letters", 1.1);
                    // Animate the letter boxes to better indicate that there aren't enough letters
                    animateInvalidWordEffect(children);
                    return;
                }

                // Concatenate all the label letters together to form the word or pseudo-word that the player has typed
                currentWord += labelText;

                // Checks if the current letter in the current word match the letter in the correct word in the same position.
                // If that is the case, decrement the number of occurrances value corresponding to that letter
                if (labelText.equals(correctWord.substring(i, i+1).toUpperCase()))
                    letterMap.merge(labelText, -1, Integer::sum);
            }

            // If the entered word is not part of the word list
            if (wordList != null && !wordList.contains(currentWord.toLowerCase())) {
                // Create a popup box displaying that it is not in the word list
                createDialog(String.format("%s is not in the word list", currentWord), 1.1);
                // Animate the letter boxes to better indicate that the word is not in the word list
                animateInvalidWordEffect(children);
                return;
            }

            // Checks if the entered word matches the correct one
            if (correctWord.toUpperCase().equals(currentWord)) {
                hasWon = true;
            }

            // If the entered word is a valid word, animate the letter boxes to flip around and indicate the letter placement (correct location, wrong location, wrong letter)
            animateFlipEffect(children);

            // Increment the current row index
            currentRowIndex++;
            return;
        }

        // Proceed only if the current key is one character long
        if (key.length() != 1) {
            return;
        }

        // Check if the key is a letter by converting to char and checking
        if (!Character.isLetter(key.charAt(0))) {
            return;
        }

        // Loop over the list of current row children and fill the first empty letter box with the typed letter
        for (Node child : currentRow.getChildren()) {
            // Explicit type conversion from Node to Label
            Label label = (Label)child;

            // Check if the label text is empty
            if (label.getText().isEmpty()) {
                // Set the label text to the key code
                label.setText(key);
                // Update the CSS ID (Updates the border color)
                label.setId("letter-box-filled");
                // Animate the letter box to better indicate that the label has been filled
                animateScalingPingPong(label, 0.08, 1.1, 1.1);
                // Break so as not to fill the rest of the labels (only the first empty one)
                break;
            }
        }
    }

    /**
     * Animates the scale factor of a node in two cycles, normally and in reverse.
     * The first cycle involves transitioning to the newly-specified scale and the second cycle
     * involves transitioning back to the original state
     * 
     * @param node The node to animate
     * @param duration The duration (in seconds) that one cycle of the animation lasts
     * @param xScale The new x-scale multiplicative factor (original is 1.0)
     * @param yScale The new y-scale multiplicative factor (original is 1.0)
     */
    private void animateScalingPingPong(Node node, double duration, double xScale, double yScale) {
        ScaleTransition transition = new ScaleTransition(Duration.seconds(duration), node);
        transition.setToX(xScale);
        transition.setToY(yScale);

        transition.setAutoReverse(true);
        transition.setCycleCount(2);

        // Transition interpolation (left in for future reference)
        // transition.setInterpolator(Interpolator.EASE_OUT);

        transition.play();
    }

    /**
     * Animates a flip animation for a list of nodes (labels) and sets the corresponding
     * letter box style and color for each label.
     * 
     * Also, handles the win animation (after the flip animation has been completed) if the current word matches the correct word.
     * Handles the lose state popup dialog.
     * Calls setKeyboardButtonColors() method after the animation to update the keyboard button colors
     * 
     * @param nodes A list of label nodes
     */
    private void animateFlipEffect(List<Node> nodes) {
        Timeline timeline = new Timeline();
        // A delay between each successive label so that all the labels do not animate synchronously,
        // but instead animate sequentially in a back-to-back fashion
        double delayOffset = 0.0;

        // The duration for one label animation
        double duration = 0.3;

        /* 
         * Reset the index position of the current letter.
         * This is used to keep track of the current label fot the setLetterBoxColor() method.
         * This is because the lambda only accepts final values as parameters,
         * so that the index must be stored as a mutable variable.
         */
        currentletterIndex = 0;

        // Loop over the list of nodes
        for (int i = 0; i < nodes.size(); i++) {
            // Explicit type conversion from Node to Label
            Label label = (Label)nodes.get(i);

            // Set the initial value as a key frame so that the successive labels do not start animating immediately
            KeyValue value0 = new KeyValue(label.scaleYProperty(), 1.0);
            KeyFrame key0 = new KeyFrame(Duration.seconds(delayOffset), value0);

            // Scale the Y down to zero to simulate the first half of the flip
            KeyValue value1 = new KeyValue(label.scaleYProperty(), 0.0);
            KeyFrame key1 = new KeyFrame(Duration.seconds(delayOffset + (duration / 2.0)), value1);

            // Update the letter box styling when not visible (y-scale set to zero)
            KeyFrame key2 = new KeyFrame(Duration.seconds(delayOffset + (duration / 2.0)), e -> { setLetterBoxColor(label); });

            // Scale the Y back to one to simulate the second half of the flip
            KeyValue value3 = new KeyValue(label.scaleYProperty(), 1.0);
            KeyFrame key3 = new KeyFrame(Duration.seconds(delayOffset + duration), value3);

            // Add all the key frames to the timeline
            timeline.getKeyFrames().addAll(key0, key1, key2, key3);

            // Increment the delay for each successive label.
            // The delay increment factor corresponds to the duration of the animation for one label
            delayOffset += duration;
        }

        // If the player has won (the current word corresponds to the correct word)
        if (hasWon) {
            // Set the keyboard button colors and animate the win effect when the current animation finishes
            timeline.setOnFinished(e -> {
                setKeyboardButtonColors();
                animateWinEffect(nodes);
            });
        }
        // If the current row is equal to the last row (The player has run out of guesses)
        else if (currentRowIndex == letterBoxChildren.size() - 1) {
            // Set the keyboard button colors and create a popup that displays the correct word when the current animation finishes
            timeline.setOnFinished(e -> {
                setKeyboardButtonColors();
                createDialog(String.format("The word was %s", correctWord), 4.5);
            });
        }
        // If the player has neither won not lost
        else {
            // Set the keyboard button colors when the current animation finishes
            timeline.setOnFinished(e -> setKeyboardButtonColors());
        }

        timeline.play();
    }

    private void setLetterBoxColor(Label label) {
        String labelText = label.getText();

        if (labelText.equals(correctWord.substring(currentletterIndex, currentletterIndex+1).toUpperCase())) {
            if (!correctLetters.contains(labelText))
                correctLetters.add(labelText);

            label.setId("letter-box-correct-location");
            label.setTextFill(Color.WHITE);
        }
        else if (correctWord.toUpperCase().contains(labelText) && letterMap.get(labelText) > 0) {
            if (!misplacedLetters.contains(labelText))
                misplacedLetters.add(labelText);

            letterMap.merge(labelText, -1, Integer::sum);

            label.setId("letter-box-wrong-location");
            label.setTextFill(Color.WHITE);
        }
        else {
            if (!wrongLetters.contains(labelText))
                wrongLetters.add(labelText);

            label.setId("letter-box-wrong-letter");
            label.setTextFill(Color.WHITE);
        }

        currentletterIndex++;
    }

    private void setKeyboardButtonColors() {
        for (Node key : keyBoxChildren) {
            Button button = (Button)key;
            String buttonText = button.getText();

            if (correctLetters.contains(buttonText)) {
                button.setId("keyboard-button-correct-location");
                button.setTextFill(Color.WHITE);
            }
            else if (misplacedLetters.contains(buttonText)) {
                button.setId("keyboard-button-wrong-location");
                button.setTextFill(Color.WHITE);
            }
            else if (wrongLetters.contains(buttonText)) {
                button.setId("keyboard-button-wrong-letter");
                button.setTextFill(Color.WHITE);
            }
        }
    }

    private void animateWinEffect(List<Node> nodes) {
        Timeline timeline = new Timeline();
        double delayOffset = 0.0;

        for (Node node : nodes) {
            KeyValue value0 = new KeyValue(node.translateYProperty(), 0);
            KeyFrame key0 = new KeyFrame(Duration.seconds(delayOffset), value0);

            KeyValue value1 = new KeyValue(node.translateYProperty(), -25);
            KeyFrame key1 = new KeyFrame(Duration.seconds(delayOffset + 0.15), value1);

            KeyValue value2 = new KeyValue(node.translateYProperty(), 12);
            KeyFrame key2 = new KeyFrame(Duration.seconds(delayOffset + 0.3), value2);

            KeyValue value3 = new KeyValue(node.translateYProperty(), -5);
            KeyFrame key3 = new KeyFrame(Duration.seconds(delayOffset + 0.45), value3);

            KeyValue value4 = new KeyValue(node.translateYProperty(), 0);
            KeyFrame key4 = new KeyFrame(Duration.seconds(delayOffset + 0.6), value4);

            timeline.getKeyFrames().addAll(key0, key1, key2, key3, key4);

            delayOffset += 0.15;
        }

        timeline.setOnFinished(e -> createDialog("Awesome! You figured out the word!", 4.5));
        timeline.play();
    }

    private void animateInvalidWordEffect(List<Node> nodes) {
        Timeline timeline = new Timeline();
        double delayOffset = 0.0;

        for (Node node : nodes) {          
            KeyValue value0 = new KeyValue(node.rotateProperty(), 0);
            KeyValue value0X = new KeyValue(node.scaleXProperty(), 1.0);
            KeyValue value0Y = new KeyValue(node.scaleYProperty(), 1.0);
            KeyFrame key0 = new KeyFrame(Duration.seconds(delayOffset), value0, value0X, value0Y);

            KeyValue value1 = new KeyValue(node.rotateProperty(), 10);
            KeyValue value1X = new KeyValue(node.scaleXProperty(), 1.05);
            KeyValue value1Y = new KeyValue(node.scaleYProperty(), 1.05);
            KeyFrame key1 = new KeyFrame(Duration.seconds(delayOffset + 0.10), value1, value1X, value1Y);

            KeyValue value2 = new KeyValue(node.rotateProperty(), -10);
            KeyValue value2X = new KeyValue(node.scaleXProperty(), 1.02);
            KeyValue value2Y = new KeyValue(node.scaleYProperty(), 1.02);
            KeyFrame key2 = new KeyFrame(Duration.seconds(delayOffset + 0.20), value2, value2X, value2Y);

            KeyValue value3 = new KeyValue(node.rotateProperty(), 0);
            KeyValue value3X = new KeyValue(node.scaleXProperty(), 1.0);
            KeyValue value3Y = new KeyValue(node.scaleYProperty(), 1.0);
            KeyFrame key3 = new KeyFrame(Duration.seconds(delayOffset + 0.25), value3, value3X, value3Y);

            timeline.getKeyFrames().addAll(key0, key1, key2, key3);

            delayOffset += 0.05;
        }

        timeline.play();
    }

    private void createDialog(String content, double duration) {
        Label popupLabel = new Label();
        popupLabel.setId("popup-label");
        popupLabel.setOpacity(0.9);
        popupLabel.setText(content);
        popupLabel.setTextFill(Color.WHITE);
        popupLabel.setFont(new Font("Lucida Sans Typewriter Bold", 14.0));
        popupLabel.setPadding(new Insets(8.0, 12.0, 8.0, 12.0));
        popupLabel.setAlignment(Pos.CENTER);

        popupList.getChildren().add(popupLabel);

        Timeline timeline = new Timeline();

        KeyFrame key0 = new KeyFrame(Duration.seconds(0.0), e -> popupLabel.setVisible(true));

        KeyValue value1 = new KeyValue(popupLabel.opacityProperty(), 0.9);
        KeyValue value1X = new KeyValue(popupLabel.scaleXProperty(), 0.0);
        KeyValue value1Y = new KeyValue(popupLabel.scaleYProperty(), 0.0);
        KeyFrame key1 = new KeyFrame(Duration.seconds(0.0), value1, value1X, value1Y);

        KeyValue value2X = new KeyValue(popupLabel.scaleXProperty(), 1.4);
        KeyValue value2Y = new KeyValue(popupLabel.scaleYProperty(), 1.4);
        KeyFrame key2 = new KeyFrame(Duration.seconds(0.1), value2X, value2Y);

        KeyValue value3X = new KeyValue(popupLabel.scaleXProperty(), 1.0);
        KeyValue value3Y = new KeyValue(popupLabel.scaleYProperty(), 1.0);
        KeyFrame key3 = new KeyFrame(Duration.seconds(0.13), value3X, value3Y);

        KeyValue value4 = new KeyValue(popupLabel.opacityProperty(), 0.9);
        KeyFrame key4 = new KeyFrame(Duration.seconds(duration), value4);

        KeyValue value5 = new KeyValue(popupLabel.opacityProperty(), 0.0);
        KeyFrame key5 = new KeyFrame(Duration.seconds(duration + 0.4), value5);

        timeline.getKeyFrames().addAll(key0, key1, key2, key3, key4, key5);
        timeline.setOnFinished(e -> popupList.getChildren().remove(0));
        timeline.play();
    }

    private void animateHelpPane(boolean reverse) {
        helpPaneBackground.setVisible(true);
        helpPane.setVisible(true);

        Timeline timeline = new Timeline();

        KeyValue value0BG = new KeyValue(helpPaneBackground.opacityProperty(), 0.0);
        KeyValue value0Y = new KeyValue(helpPane.translateYProperty(), 0);
        KeyValue value0 = new KeyValue(helpPane.opacityProperty(), 0.0);
        KeyFrame key0 = new KeyFrame(Duration.seconds(0.0), value0BG, value0Y, value0);

        KeyValue value1 = new KeyValue(helpPaneBackground.opacityProperty(), 0.35);
        KeyFrame key1 = new KeyFrame(Duration.seconds(0.1), value1);

        KeyValue value2 = new KeyValue(helpPane.opacityProperty(), 1.0);
        KeyValue value2Y = new KeyValue(helpPane.translateYProperty(), -45);
        KeyFrame key2 = new KeyFrame(Duration.seconds(0.24), value2, value2Y);

        timeline.getKeyFrames().addAll(key0, key1, key2);

        if (reverse) {
            timeline.setRate(-1.0);
            timeline.setOnFinished(e -> {
                helpPaneBackground.setVisible(false);
                helpPane.setVisible(false);
            });
        }

        timeline.play();
    }

    private void createAlertDialogBox(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("icon.png").toString()));

        Platform.runLater(alert::showAndWait);

    }

}
