package octavo.cmsc124.lolcode_program.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import octavo.cmsc124.lolcode_program.LolCodeMain;
import octavo.cmsc124.lolcode_program.model.Lexeme;
import octavo.cmsc124.lolcode_program.model.LexicalAnalyzer;
import octavo.cmsc124.lolcode_program.model.SyntaxAnalyzer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class GuiController implements Initializable {
    @FXML private Menu aboutMenu;
    @FXML private TextArea codeEditor;
    @FXML private Button executeBtn;
    @FXML private MenuItem exitMenu;
    @FXML private Menu fileMenu;
    @FXML private Label fileName;
    @FXML private Button findFileBtn;
    @FXML private MenuItem newMenu;
    @FXML private MenuItem openMenu;
    @FXML private Label outputPane;
    @FXML private MenuItem saveAsMenu;
    @FXML private MenuItem saveMenu;
    @FXML public TableView<Lexeme> lexemeTable;
    @FXML private TableView<?> symbolTable;
    @FXML private TableColumn<Lexeme, String> lexemeColTable;
    @FXML private TableColumn<Lexeme, String> classificationColTable;

    @FXML private ObservableList<Lexeme> lexemesObservableList = FXCollections.observableArrayList();


//    @FXML private TableColumn<String, String> valueColTable;
//    @FXML private TableColumn<String, String> identifierColTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lexemeColTable.setCellValueFactory(new PropertyValueFactory<Lexeme, String>("token"));
        classificationColTable.setCellValueFactory(new PropertyValueFactory<Lexeme, String>("classification"));
//        valueColTable.setCellValueFactory(new PropertyValueFactory<String, String>("value"));
//        identifierColTable.setCellValueFactory(new PropertyValueFactory<String, String>("identifier"));
//        lexemeTable.setItems(lexemesObservableList);
        outputPane.setText("");

        executeBtn.disableProperty().bind(codeEditor.textProperty().isEmpty());
    }

    @FXML
    void exitProgram(ActionEvent event) {

    }

    @FXML
    void onExecuteCode(ActionEvent event) {
        lexemeTable.getItems().clear();


        String [] lines = codeEditor.getText().split("\n");
        Map<Integer, String> code = new HashMap<>();
        boolean lock = true; // To ignore multiple lines comments

        for(int i = 0; i < lines.length; i++){
            String line = lines[i];
            if(!line.strip().startsWith("BTW") && lock && !line.strip().startsWith("OBTW") && !line.isBlank())
                code.put(i+1, line);
            if(line.strip().startsWith("OBTW"))
                lock = false;
            if(line.strip().endsWith("TLDR"))
                lock = true;
        }

        List<Lexeme> lexemes = new LexicalAnalyzer().analyzeCode(code);

        lexemesObservableList.addAll(lexemes);
        lexemeTable.setItems(lexemesObservableList);

        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexemes);
        syntaxAnalyzer.analyze();


//        lexemeColTable.setItems(FXCollections.observableList(lexemes));
    }

    @FXML
    void onFindFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick a file");
        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userHome));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("LolCode Files", "*.lol"));
        File file = fileChooser.showOpenDialog(LolCodeMain.stage.getOwner());

        fileName.setText(file.getName());

        if(file != null){
            Files.lines(file.toPath(), Charset.forName("UTF-8"))
                    .forEach(line -> codeEditor.appendText(line.concat("\n")));
        }
    }

}
