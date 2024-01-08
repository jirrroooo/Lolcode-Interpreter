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
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
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

    public static boolean hasSyntaxError = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lexemeColTable.setCellValueFactory(new PropertyValueFactory<Lexeme, String>("lexeme"));
        classificationColTable.setCellValueFactory(new PropertyValueFactory<Lexeme, String>("typeStr"));

        identifierColTable.setCellValueFactory(new PropertyValueFactory<Variable, String>("varName"));
        valueColTable.setCellValueFactory(new PropertyValueFactory<Variable, Object>("varValue"));

        staticOutputPane = outputPane;

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

        List<Map.Entry<Integer, String>> codeList = new ArrayList<>(code.entrySet());
        codeList.sort(Map.Entry.comparingByKey());

        List<Lexeme> lexemes = new LexicalAnalyzer().analyzeCode(codeList);
        lexemesObservableList.addAll(lexemes);
        lexemeTable.setItems(lexemesObservableList);

        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexemes);

        syntaxAnalyzer.start();

        try {
            syntaxAnalyzer.join();
        }catch (InterruptedException e){
            hasSyntaxError = true;
        }


        if(!hasSyntaxError){
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(lexemes);
            semanticAnalyzer.start();

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

    @FXML
    void newFile() {
        fileName.setText("*new_code_1.lol");

        codeEditor.clear();
    }

    @FXML
    void saveFile(){
        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();

        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userHome));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("LolCode Files", "*.lol"));
        // Show save dialog
        File file = fileChooser.showSaveDialog(LolCodeMain.stage);

        try {
            try (FileWriter writer = new FileWriter(file)) {
                // Write content to the file
                writer.write(codeEditor.getText());
                System.out.println("File saved successfully: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error saving file: " + e.getMessage());
            }
        }catch (NullPointerException e){
            outputPane.setText("Saving File Cancelled");
        }

    }
}
