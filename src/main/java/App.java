
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public static Scene scene;
    // A reference to the AppController
    private static AppController controller;

    // The minimum width and height of the main Application window
    private static final int minWidth = 480;
    private static final int minHeight = 720;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * 
     * <p>
     * 
     * NOTE: This method is called on the JavaFX Application Thread.
     * 
     * @param stage the primary stage for this application, onto which the application scene can be set.
     * Applications may create other stages, if needed, but they will not be primary stages. 
     * @throws IOException
     */
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

    /**
     * Loads the specified fxml file as the root parent node of the scene (containing all the children nodes).
     * Stores a reference to the AppController instance to access in the start method
     * 
     * @param fxml A string containing the name of the fxml file to load (without the extension)
     * @return The Parent root that is the base class for all nodes that have children in the scene graph.
     * @throws IOException
     */
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