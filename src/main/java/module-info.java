module com.example.kinomanager {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;

    opens com.example.kinomanager to javafx.fxml;
    opens com.example.kinomanager.controller to javafx.fxml;
    opens com.example.kinomanager.model to javafx.base, com.google.gson;
    opens com.example.kinomanager.dao to com.google.gson;

    exports com.example.kinomanager;
    exports com.example.kinomanager.controller;
    exports com.example.kinomanager.model;
    exports com.example.kinomanager.dao;
    exports com.example.kinomanager.service;
}