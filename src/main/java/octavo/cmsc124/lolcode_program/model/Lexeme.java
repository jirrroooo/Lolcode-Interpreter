package octavo.cmsc124.lolcode_program.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Lexeme {
    private StringProperty token = new SimpleStringProperty();
    private StringProperty classification = new SimpleStringProperty();

    public Lexeme(String token, String classification){
        setToken(token);
        setClassification(classification);
    }

    public String getToken() {
        return token.get();
    }

    public StringProperty tokenProperty() {
        return token;
    }

    public void setToken(String token) {
        this.token.set(token);
    }


    public String getClassification() {
        return classification.get();
    }

    public StringProperty setClassificationProperty() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification.set(classification);
    }
}
