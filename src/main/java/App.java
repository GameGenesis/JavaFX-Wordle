
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        stage.setTitle("Wordle");
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent parent = fxmlLoader.load();

        controller = (AppController)fxmlLoader.getController();

        return parent;
    }

    public static void main(String[] args) {
        launch();
    }

}