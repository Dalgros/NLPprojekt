package com.example.nlpprojekt;

import com.example.nlpprojekt.wiki.WikiArticle;
import com.example.nlpprojekt.wiki.WikipediaManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        List<WikiArticle> biologyArticles = WikipediaManager.getWikiArticles("https://en.wikipedia.org/wiki/Biology", 2,2000);
        launch();
    }
}