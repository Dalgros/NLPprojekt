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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

        HashMap<String, AtomicInteger> inputBoW = ArticleManager.parseTextAndGetBOW(searchInput.getText());

        Text textBeforeLink = new Text("Visit the following link: \n");

        // Create a Hyperlink
        Hyperlink link1 = new Hyperlink("https://en.wikipedia.org/wiki/Computer");
        Hyperlink link2 = new Hyperlink("https://en.wikipedia.org/wiki/Biology");

        link1.setOnAction(openLinkHandler);
        link2.setOnAction(openLinkHandler);

        Text textAfterLink = new Text("\n for more information.");
        resultText.getChildren().addAll(textBeforeLink, link1, link2, textAfterLink);
    }


}