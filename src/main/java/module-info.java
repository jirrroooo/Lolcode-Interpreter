module com.example.test_javafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens octavo.cmsc124.lolcode_program to javafx.fxml;
    exports octavo.cmsc124.lolcode_program;
}