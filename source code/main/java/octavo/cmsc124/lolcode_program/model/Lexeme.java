package octavo.cmsc124.lolcode_program.model;

import javafx.beans.property.*;

import java.util.Locale;


public class Lexeme {
    private StringProperty token = new SimpleStringProperty();
    private ObjectProperty<LexemeType> type = new SimpleObjectProperty<>();

    private int lineNumber;
    private String typeStr;

    public Lexeme(String token, LexemeType type, int lineNumber){
        this.token.set(token);
        this.type.set(type);
        this.lineNumber = lineNumber;
        this.typeStr = getStringType();
    }

    public LexemeType getType() {
        return type.get();
    }

    public String getLexeme() {
        return token.get();
    }

    public String getTypeStr() {
        return typeStr;
    }

    public int getLineNumber(){
        return lineNumber;
    }

    public String getStringType(){
        String typeStr = type.get().toString();
        typeStr = typeStr.toLowerCase();
        typeStr = typeStr.replace("_", " ");

        return typeStr;
    }
}

