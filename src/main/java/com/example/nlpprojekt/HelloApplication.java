package com.example.nlpprojekt;

import com.example.nlpprojekt.wiki.ArticleManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("Wyszukiwarka dokumentów");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException {
        ArticleManager.loadVectors();
        ArticleManager.addWikiArticles("https://en.wikipedia.org/wiki/Biology", 0,2000);
        ArticleManager.addWikiArticles("https://en.wikipedia.org/wiki/History", 0,2000);
        ArticleManager.addWikiArticles("https://en.wikipedia.org/wiki/Chemistry", 0,2000);
        ArticleManager.saveVectorsToFile();
        launch();
    }
}