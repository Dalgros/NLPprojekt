package com.example.nlpprojekt;

import com.example.nlpprojekt.wiki.ArticleManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;

public class MainController {
    @FXML
    public TextArea resultText;
    @FXML
    public TextField linkInput, searchInput;
    @FXML
    public Spinner<Integer> levelSpinner, maxSpinner;
    @FXML
    public TextFlow textFlow;
    @FXML
    public ProgressBar progressBar;

    @FXML
    private void initialize()
    {
        levelSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0, 1));
        maxSpinner.setValueFactory( new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 10, 10));
        textFlow.getChildren().add(new Text("Ilość dokumentów w bazie: " + ArticleManager.getNumberOfArticles()));
    }
    @FXML
    public void AddArticles(ActionEvent actionEvent) throws IOException {
        //https://en.wikipedia.org/wiki/Computer
        int num = ArticleManager.addWikiArticles(linkInput.getText(), levelSpinner.getValue(), maxSpinner.getValue(), progressBar);
        linkInput.setText("");
        textFlow.getChildren().clear();
        textFlow.getChildren().add(new Text("Ilość dokumentów w bazie: " + ArticleManager.getNumberOfArticles()));

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText("Pomyślnie dodano następującą liczbę artykółów: " + num );
        alert.showAndWait();
    }

    @FXML
    public void search(ActionEvent actionEvent) {
        resultText.setText("The best result is: " + searchInput.getText());
    }


}