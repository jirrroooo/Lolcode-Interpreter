package octavo.cmsc124.lolcode_program.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Variable {
    private StringProperty varName = new SimpleStringProperty();
    private ObjectProperty<Object> varValue = new SimpleObjectProperty<>();
    private ObjectProperty<DataType> varType = new SimpleObjectProperty<>();


    public Variable(String varName, Object varValue, DataType varType){
        this.varName.setValue(varName);
        this.varValue.setValue(varValue);
        this.varType.setValue(varType);
    }

    public String getVarName() {
        return varName.get();
    }

    public StringProperty varNameProperty() {
        return varName;
    }

    public Object getVarValue() {
        return varValue.get();
    }

    public ObjectProperty<Object> varValueProperty() {
        return varValue;
    }

    public DataType getVarType() {
        return varType.get();
    }

    public ObjectProperty<DataType> varTypeProperty() {
        return varType;
    }
}
