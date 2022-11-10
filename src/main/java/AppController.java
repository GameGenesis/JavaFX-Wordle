import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

    // A list of Buttons that are contained within the HBoxes in keyBox
    private List<Node> keyBoxChildren;
    // A list of the revealed letters that are in the correct location in the word
    private List<String> correctLetters;
    // A list of the revealed letters that are in the incorrect location in the word
    private List<String> misplacedLetters;
    // A list of the revealed letters that are not in the word
    private List<String> wrongLetters;

    // The index of the label in the current row to update the color of
    private int currentletterIndex = 0;
    // The word that was submitted as an attempt (The word that the user typed on the current row)
    private String currentWord = "";

    // The correct word
    private String correctWord = "Rainy";

    // Specifies whether the win animation should be played and whether user input should be disabled (if the player has won)
    private boolean hasWon = false;

    @FXML
    private void initialize() {
        letterBoxChildren = letterBox.getChildren();

        keyBoxChildren = new ArrayList<>();
        List<Node> hboxChildren = keyBox.getChildren();

        for (Node child : hboxChildren) {
            HBox hbox = (HBox)child;
            keyBoxChildren.addAll(hbox.getChildren());
        }

        correctLetters = new ArrayList<>();
        misplacedLetters = new ArrayList<>();
        wrongLetters = new ArrayList<>();
    }

    // Called when an onscreen keyboard button is pressed
    @FXML
    private void keyPressed(ActionEvent event) {
        Object node = event.getSource();
        Button button = (Button)node;
        getInput(button.getText());
    }

    public void getInput(String key) {
        if (letterBoxChildren == null || currentRowIndex > letterBoxChildren.size() - 1 || hasWon) {
            return;
        }

        currentRow = (HBox)letterBoxChildren.get(currentRowIndex);

        if (key.equals("BACK_SPACE")) {
            List<Node> children = currentRow.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                Label label = (Label)children.get(i);

                if (!label.getText().isEmpty()) {
                    label.setText("");
                    label.setId("letter-box-empty");
                    label.setTextFill(Color.web("#383838"));
                    animateScalingPingPong(label, 0.07, 0.9, 0.9);
                    break;
                }
            }
            return;
        }

        if (key.equals("ENTER")) {
            List<Node> children = currentRow.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Label label = (Label)children.get(i);
                String labelText = label.getText();

                if (labelText.isEmpty()) {
                    return;
                }

                currentWord += labelText;
            }

            if (correctWord.toUpperCase().equals(currentWord)) {
                hasWon = true;
            }

            animateFlipEffect(children);

            currentRowIndex++;
            currentWord = "";
            return;
        }

        if (key.length() != 1) {
            return;
        }

        char[] letters = key.toCharArray();
        if (!Character.isLetter(letters[0])) {
            return;
        }

        for (Node child : currentRow.getChildren()) {
            Label label = (Label)child;

            if (label.getText().isEmpty()) {
                label.setText(key);
                label.setId("letter-box-filled");
                animateScalingPingPong(label, 0.08, 1.1, 1.1);
                break;
            }
        }
    }

    private void animateScalingPingPong(Node node, double duration, double xScale, double yScale) {
        ScaleTransition transition = new ScaleTransition(Duration.seconds(duration), node);
        transition.setToX(xScale);
        transition.setToY(yScale);

        transition.setAutoReverse(true);
        transition.setCycleCount(2);
        // transition.setInterpolator(Interpolator.EASE_OUT);

        transition.play();
    }

    private void animateFlipEffect(List<Node> nodes) {
        Timeline timeline = new Timeline();
        double delayOffset = 0.0;

        currentletterIndex = 0;

        for (int i = 0; i < nodes.size(); i++) {
            Label label = (Label)nodes.get(i);

            KeyValue value0 = new KeyValue(label.scaleYProperty(), 1.0);
            KeyFrame key0 = new KeyFrame(Duration.seconds(delayOffset), value0);

            KeyValue value1 = new KeyValue(label.scaleYProperty(), 0.0);
            KeyFrame key1 = new KeyFrame(Duration.seconds(delayOffset + 0.15), value1);

            KeyFrame key2 = new KeyFrame(Duration.seconds(delayOffset + 0.15), e -> { setLetterBoxColor(label); });

            KeyValue value3 = new KeyValue(label.scaleYProperty(), 1.0);
            KeyFrame key3 = new KeyFrame(Duration.seconds(delayOffset + 0.3), value3);

            timeline.getKeyFrames().addAll(key0, key1, key2, key3);

            delayOffset += 0.3;
        }

        if (hasWon) {
            timeline.setOnFinished(e -> {
                setKeyboardButtonColors();
                animateWinEffect(nodes);
            });
        }
        else if (currentRowIndex == letterBoxChildren.size() - 1) {
            timeline.setOnFinished(e -> {
                setKeyboardButtonColors();
                createDialog("Incorrect Guess", String.format("You did not guess the word! The word was %s", correctWord));
            });
        }
        else {
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
        else if (correctWord.toUpperCase().contains(labelText)) {
            if (!misplacedLetters.contains(labelText))
                misplacedLetters.add(labelText);

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

        timeline.setOnFinished(e -> createDialog("Correct Guess", "Awesome! You figured out the word!"));
        timeline.play();
    }

    private void createDialog(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Platform.runLater(alert::showAndWait);
    }

}
