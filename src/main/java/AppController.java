import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private List<Object> wordList;
    // The correct word
    private String correctWord = "Rainy";

    // Specifies whether the win animation should be played and whether user input should be disabled (if the player has won)
    private boolean hasWon = false;

    @FXML
    private VBox popupList;

    @FXML
    private Pane helpPaneBackground;

    @FXML
    private VBox helpPane;

    @FXML
    private void initialize() {
        getWord();
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

    @FXML
    private void showHelpPane(MouseEvent event) {
        animateHelpPane(false);
    }

    @FXML
    private void hideHelpPane(MouseEvent event) {
        animateHelpPane(true);
    }

    private void getWord() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("src/main/resources/WordList.json")));
            JSONObject obj = new JSONObject(content);
            JSONArray wordArray = obj.getJSONArray("wordList");
            correctWord = wordArray.getString(new Random().nextInt(wordArray.length()));

            wordList = wordArray.toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println(correctWord);
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
            currentWord = "";

            List<Node> children = currentRow.getChildren();

            for (int i = 0; i < children.size(); i++) {
                Label label = (Label)children.get(i);
                String labelText = label.getText();

                if (labelText.isEmpty()) {
                    createDialog("Not enough letters", 1.1);
                    animateInvalidWordEffect(children);
                    return;
                }

                currentWord += labelText;
            }

            // Checks if the entered text forms a real word
            if (!wordList.contains(currentWord.toLowerCase())) {
                createDialog(String.format("%s is not in the word list", currentWord), 1.1);
                animateInvalidWordEffect(children);
                return;
            }

            // Checks if the entered word matches the correct one
            if (correctWord.toUpperCase().equals(currentWord)) {
                hasWon = true;
            }

            animateFlipEffect(children);

            currentRowIndex++;
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
                createDialog(String.format("The word was %s", correctWord), 4.5);
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
        popupLabel.setFont(new Font("Lucida Sans Demibold Roman", 14.0));
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
