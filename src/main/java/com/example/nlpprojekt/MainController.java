package com.example.nlpprojekt;

import com.example.nlpprojekt.wiki.ArticleManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    public TextField linkInput, searchInput;
    @FXML
    public Spinner<Integer> levelSpinner, maxSpinner;
    @FXML
    public TextFlow docSummaryTextFlow, resultText;
    @FXML
    public ProgressBar progressBar;

    @FXML
    private void initialize()
    {
        levelSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0, 1));
        maxSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 10, 10));
        docSummaryTextFlow.getChildren().add(new Text("Ilość dokumentów w bazie: " + ArticleManager.getNumberOfArticles()));

    }
    @FXML
    public void AddArticles(ActionEvent actionEvent) throws IOException {
        //https://en.wikipedia.org/wiki/Computer
        int num = ArticleManager.addWikiArticles(linkInput.getText(), levelSpinner.getValue(), maxSpinner.getValue(), progressBar);
        linkInput.setText("");
        docSummaryTextFlow.getChildren().clear();
        docSummaryTextFlow.getChildren().add(new Text("Ilość dokumentów w bazie: " + ArticleManager.getNumberOfArticles()));

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText("Pomyślnie dodano następującą liczbę artykółów: " + num );
        alert.showAndWait();
    }

    @FXML
    public void search(ActionEvent actionEvent) {
        EventHandler<ActionEvent> openLinkHandler = event -> {
            Hyperlink source = (Hyperlink) event.getSource(); // Get the clicked hyperlink
            MainApplication.hostServices.showDocument(source.getText()); // Open the hyperlink's text as a URL
        };

        Map<String, Double> similarArticles = ArticleManager.calculateSimilaritiesToInput(searchInput.getText());

        Map<String, Double> sortedMap = new LinkedHashMap<>();
        similarArticles.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

        Text textBeforeLink = new Text("The most similar articles are the following:");
        resultText.getChildren().add(textBeforeLink);

        for(Map.Entry<String, Double> articleEntry : sortedMap.entrySet()) {
            Text entryText = new Text("\nSim: " + String.format("%.5f", articleEntry.getValue()) + " | ");
            Hyperlink link = new Hyperlink(articleEntry.getKey());
            link.setOnAction(openLinkHandler);
            resultText.getChildren().addAll(entryText, link);
        }
    }


}