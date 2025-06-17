module DamaProject {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    opens client;
    exports client;
    opens server;
    exports server;
    opens model;
    exports model;
}