
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    public static Scene scene;
    private static AppController controller;

    private static final int minWidth = 480;
    private static final int minHeight = 720;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("wordle"), minWidth, minHeight);
        scene.getStylesheets().add("stylesheet.css");

        // Allows the controller class to get keyboard input from the user
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (controller != null)
                controller.getInput(key.getCode().name());
        });

        stage.setTitle("Wordle");
        stage.getIcons().add(new Image("icon.png"));

        // Set stage min size and specify that it is resizable
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setResizable(true);

        stage.setScene(scene);

        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent parent = fxmlLoader.load();

        // Store a reference to the AppController instance to access in the start method
        controller = (AppController)fxmlLoader.getController();

        return parent;
    }

    public static void main(String[] args) {
        launch();
    }

}