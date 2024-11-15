module sae.statisalle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens sae.statisalle to javafx.fxml;
    exports sae.statisalle;

    opens sae.statisalle.controller to javafx.fxml;
    exports sae.statisalle.controller;

    opens sae.statisalle.exception to javafx.fxml;
    exports sae.statisalle.exception;
}
