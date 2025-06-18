module DamaProject {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    opens it.polimi.client;
    exports it.polimi.client;
    opens it.polimi.server;
    exports it.polimi.server;
    opens it.polimi.model;
    exports it.polimi.model;
}