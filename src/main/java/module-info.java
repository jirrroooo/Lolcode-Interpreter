module octavo.cmsc124.lolcode_program {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens octavo.cmsc124.lolcode_program to javafx.fxml, javafx.base;
    exports octavo.cmsc124.lolcode_program;
    exports octavo.cmsc124.lolcode_program.controller;
    exports octavo.cmsc124.lolcode_program.model to javafx.fxml, javafx.base;
    opens octavo.cmsc124.lolcode_program.controller to javafx.fxml, javafx.base;
    opens octavo.cmsc124.lolcode_program.model to javafx.fxml, javafx.base;
}