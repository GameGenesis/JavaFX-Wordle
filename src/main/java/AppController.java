import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class AppController {

    @FXML
    private AnchorPane lineAnchor;

    @FXML
    private HBox row1;

    public void getInput(String key) {
        if (key.equals("BACK_SPACE")) {
            List<Node> children = row1.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                Label label = (Label)children.get(i);

                if (!label.getText().isEmpty()) {
                    label.setText("");
                    break;
                }
            }
            return;
        }

        if (key.equals("ENTER")) {
            for (Node child : row1.getChildren()) {
                Label label = (Label)child;
                
                if (label.getText().isEmpty()) {
                    return;
                }
            }
            
            System.out.println("Complete Word!");
            return;
        }

        if (key.length() != 1) {
            return;
        }

        char[] letters = key.toCharArray();
        if (!Character.isLetter(letters[0])) {
            return;
        }

        for (Node child : row1.getChildren()) {
            Label label = (Label)child;
            
            if (label.getText().isEmpty()) {
                label.setText(key);
                break;
            }
        }
    }

}
