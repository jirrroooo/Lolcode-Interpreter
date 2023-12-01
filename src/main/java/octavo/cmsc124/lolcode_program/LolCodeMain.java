package octavo.cmsc124.lolcode_program;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LolCodeMain extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(LolCodeMain.class.getResource("LolcodeGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 560);
        primaryStage.setTitle("John Rommel Octavo's Lolcode Interpreter");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}