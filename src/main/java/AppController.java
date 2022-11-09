import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class AppController {

    @FXML
    private AnchorPane lineAnchor;

    @FXML
    private VBox letterBox;

    private HBox rowBox;

    private int currentRow = 0;
    private String correctWord = "rainy";

    public void getInput(String key) {
        List<Node> letterBoxChildren = letterBox.getChildren();
        if (currentRow > letterBoxChildren.size() - 1)
            return;

        rowBox = (HBox)letterBoxChildren.get(currentRow);

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

                if (labelText.equals(correctWord.substring(i, i+1).toUpperCase())) {
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
            }
            
            System.out.println("Complete Word!");
            if (currentRow < 5)
                currentRow++;
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
                break;
            }
        }
    }

}
