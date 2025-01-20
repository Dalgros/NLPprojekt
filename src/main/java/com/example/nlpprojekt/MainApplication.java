package com.example.nlpprojekt;

import com.example.nlpprojekt.wiki.ArticleManager;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    public static HostServices hostServices;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 580);
        stage.setTitle("Wyszukiwarka dokumentów");
        stage.setScene(scene);
        stage.show();

        hostServices = getHostServices();
    }

    public static void main(String[] args) throws IOException {
        ArticleManager.loadVectors();
        launch();
        ArticleManager.saveVectorsToFiles();
    }
}