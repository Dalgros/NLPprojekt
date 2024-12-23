module com.example.nlpprojekt {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.jsoup;
    requires edu.stanford.nlp.corenlp;
    exports com.example.nlpprojekt;
    opens com.example.nlpprojekt to javafx.fxml;
    exports com.example.nlpprojekt.wiki;
    opens com.example.nlpprojekt.wiki to javafx.fxml;
}