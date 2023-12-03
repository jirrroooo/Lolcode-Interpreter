package octavo.cmsc124.lolcode_program.model;

import javafx.beans.property.*;


public class Lexeme {
    private StringProperty token = new SimpleStringProperty();
    private ObjectProperty<LexemeType> type = new SimpleObjectProperty<>();

    public Lexeme(String token, LexemeType type){
        this.token.set(token);
        this.type.set(type);
    }

    public LexemeType getType() {
        return type.get();
    }

    public String getLexeme() {
        return token.get();
    }

    // ==================================================================

    public String getToken() {
        return token.get();
    }

    public StringProperty tokenProperty() {
        return token;
    }

    public void setToken(String token) {
        this.token.set(token);
    }


    public LexemeType getClassification() {
        return type.get();
    }
    }

