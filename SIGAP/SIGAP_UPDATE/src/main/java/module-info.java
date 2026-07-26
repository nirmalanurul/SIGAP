module com.sigap {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;
    requires net.sf.jasperreports.pdf;
    requires java.desktop;
    requires net.sf.jasperreports.core;

    opens com.sigap to javafx.fxml;
    opens com.sigap.controller to javafx.fxml;
    opens com.sigap.ADT to javafx.base;

    exports com.sigap;
    exports com.sigap.controller;
    exports com.sigap.database;
    exports com.sigap.ADT;
}