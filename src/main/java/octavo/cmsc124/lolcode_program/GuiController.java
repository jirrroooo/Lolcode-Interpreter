package octavo.cmsc124.lolcode_program;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;

public class GuiController {

    @FXML
    private Menu aboutMenu;

    @FXML
    private TextArea codeEditor;

    @FXML
    private Button executeBtn;

    @FXML
    private MenuItem exitMenu;

    @FXML
    private Menu fileMenu;

    @FXML
    private Label fileName;

    @FXML
    private Button findFileBtn;



    @FXML
    private MenuItem newMenu;

    @FXML
    private MenuItem openMenu;

    @FXML
    private Label outputPane;

    @FXML
    private MenuItem saveAsMenu;

    @FXML
    private MenuItem saveMenu;

    @FXML
    private TableColumn<?, ?> valueColTable;

    @FXML
    private TableColumn<?, ?> classificationColTable;

    @FXML
    private TableColumn<?, ?> identifierColTable;

    @FXML
    private TableColumn<?, ?> lexemeColTable;

    @FXML
    void exitProgram(ActionEvent event) {

    }

    @FXML
    void onExecuteCode(ActionEvent event) {

    }

    @FXML
    void onFindFile(ActionEvent event) {

    }

}
