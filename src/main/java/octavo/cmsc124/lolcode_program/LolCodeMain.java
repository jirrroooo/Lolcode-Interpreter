package octavo.cmsc124.lolcode_program;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LolCodeMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LolCodeMain.class.getResource("lolcode_gui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 560);
        stage.setTitle("John Rommel Octavo's Lolcode Interpreter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}