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
