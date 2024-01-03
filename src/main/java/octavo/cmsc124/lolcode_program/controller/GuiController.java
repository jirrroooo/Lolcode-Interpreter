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
import octavo.cmsc124.lolcode_program.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class GuiController implements Initializable {
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
    private TextArea outputPane;
    @FXML
    public static TextArea staticOutputPane;
    @FXML
    private MenuItem saveAsMenu;
    @FXML
    private MenuItem saveMenu;
    @FXML
    public TableView<Lexeme> lexemeTable;
    @FXML
    private TableView<Variable> symbolTable;
    @FXML
    private TableColumn<Lexeme, String> lexemeColTable;
    @FXML
    private TableColumn<Lexeme, String> classificationColTable;
    @FXML
    private TableColumn<Variable, String> identifierColTable;
    @FXML
    private TableColumn<Variable, Object> valueColTable;

    @FXML
    private ObservableList<Lexeme> lexemesObservableList = FXCollections.observableArrayList();

    private ObservableList<Variable> variableObservableList = FXCollections.observableArrayList();
    public static ObservableList<Variable> staticVariableObservableList = FXCollections.observableArrayList();

    private Map<String, Object> variableTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lexemeColTable.setCellValueFactory(new PropertyValueFactory<Lexeme, String>("lexeme"));
        classificationColTable.setCellValueFactory(new PropertyValueFactory<Lexeme, String>("typeStr"));

        identifierColTable.setCellValueFactory(new PropertyValueFactory<Variable, String>("varName"));
        valueColTable.setCellValueFactory(new PropertyValueFactory<Variable, Object>("varValue"));

        staticOutputPane = outputPane;
//        staticVariableObservableList = variableObservableList;
//        symbolTable.setItems(variableObservableList);

        executeBtn.disableProperty().bind(codeEditor.textProperty().isEmpty());
    }

    @FXML
    void exitProgram(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void onExecuteCode(ActionEvent event) {
        lexemeTable.getItems().clear();
        symbolTable.getItems().clear();
        outputPane.clear();


        String[] lines = codeEditor.getText().split("\n");

        Map<Integer, String> code = new HashMap<>();
        boolean lock = true; // To ignore multiple lines comments

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.strip().startsWith("BTW") && lock && !line.strip().startsWith("OBTW") && !line.isBlank()) {
                code.put(i + 1, line);
            }
            if (line.strip().startsWith("OBTW"))
                lock = false;
            if (line.strip().startsWith("TLDR"))
                lock = true;
        }

//        System.out.println(code);

        List<Map.Entry<Integer, String>> codeList = new ArrayList<>(code.entrySet());
        codeList.sort(Map.Entry.comparingByKey());

//        System.out.println(codeList);

        boolean hasSyntaxError = false;

        List<Lexeme> lexemes = new LexicalAnalyzer().analyzeCode(codeList);
        lexemesObservableList.addAll(lexemes);
        lexemeTable.setItems(lexemesObservableList);

        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexemes);
        syntaxAnalyzer.start();

        try {
            syntaxAnalyzer.join();
        } catch (InterruptedException e) {
            hasSyntaxError = true;
        }

        if(!hasSyntaxError){
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(lexemes);
            semanticAnalyzer.start();

//            try {
//                // Using join to wait for thread1 to finish
//                semanticAnalyzer.join();
//                symbolTable.setItems(staticVariableObservableList);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            symbolTable.setItems(staticVariableObservableList);

        }

    }

    @FXML
    void onFindFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick a file");
        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userHome));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("LolCode Files", "*.lol"));
        File file = fileChooser.showOpenDialog(LolCodeMain.stage.getOwner());

        try{
            fileName.setText(file.getName());
        }catch (NullPointerException e){
            outputPane.setText("Cancelled File Selection");
        }

        codeEditor.clear();

        try{
            Files.lines(file.toPath(), StandardCharsets.UTF_8)
                    .forEach(line -> codeEditor.appendText(line.concat("\n")));
        }catch (NullPointerException e){
            outputPane.setText("Cancelled File Selection");
        }
    }
}
