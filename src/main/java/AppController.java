import java.util.List;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AppController {

    @FXML
    private AnchorPane lineAnchor;

    @FXML
    private VBox letterBox;

    private HBox rowBox;

    private int currentRowIndex = 0;
    private int currentletterIndex = 0;

    private String correctWord = "rainy";

    public void getInput(String key) {
        List<Node> letterBoxChildren = letterBox.getChildren();
        if (currentRowIndex > letterBoxChildren.size() - 1)
            return;

        rowBox = (HBox)letterBoxChildren.get(currentRowIndex);

        if (key.equals("BACK_SPACE")) {
            List<Node> children = rowBox.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                Label label = (Label)children.get(i);

                if (!label.getText().isEmpty()) {
                    label.setText("");
                    label.setId("letter-box-empty");
                    label.setTextFill(Color.BLACK);
                    break;
                }
            }
            return;
        }

        if (key.equals("ENTER")) {
            List<Node> children = rowBox.getChildren();
            for (int i = children.size() - 1; i >= 0 ; i--) {
                Label label = (Label)children.get(i);
                String labelText = label.getText();

                if (labelText.isEmpty()) {
                    return;
                }
                // animateScalingPingPong(label, 0.2, 1.0, 0.0);
            }

            System.out.println("Complete Word!");
            animateFlipEffect(children);
            // animateWinEffect(children);

            if (currentRowIndex < 5)
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

        for (Node child : rowBox.getChildren()) {
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

            KeyFrame key2 = new KeyFrame(Duration.seconds(delayOffset + 0.15), e -> { setLetterBoxColor(e, label); });

            KeyValue value3 = new KeyValue(label.scaleYProperty(), 1.0);
            KeyFrame key3 = new KeyFrame(Duration.seconds(delayOffset + 0.3), value3);

            timeline.getKeyFrames().addAll(key0, key1, key2, key3);

            delayOffset += 0.3;
        }

        timeline.play();
    }

    private void setLetterBoxColor(ActionEvent e, Label label) {
        String labelText = label.getText();

        if (labelText.equals(correctWord.substring(currentletterIndex, currentletterIndex+1).toUpperCase())) {
            label.setId("letter-box-correct-location");
            label.setTextFill(Color.WHITE);
        }
        else if (correctWord.toUpperCase().contains(labelText)) {
            label.setId("letter-box-wrong-location");
            label.setTextFill(Color.WHITE);
        }
        else {
            label.setId("letter-box-wrong-letter");
            label.setTextFill(Color.WHITE);
        }

        currentletterIndex++;
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

        timeline.play();
    }

}
