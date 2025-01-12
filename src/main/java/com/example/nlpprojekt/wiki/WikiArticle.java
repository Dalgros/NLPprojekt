package com.example.nlpprojekt.wiki;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nd4j.linalg.api.ndarray.INDArray;

public class WikiArticle {

    private static String WIKI_URL_BASE = "https://en.wikipedia.org/";
    private String link;
    private String text;
    private List<String> nextArticles = new ArrayList<>();
    private Map<String, AtomicInteger> BoW;
    private Map<String, Double> tfidf;

    private INDArray word2VecSummedVector;

    public WikiArticle(String link) throws IOException {
        this.link = link;

        //get the html content of wiki page
        Document doc = Jsoup.connect(link).get();

        //transform html to plain content of article's text and get related articles
        Elements paragraphs = doc.select(".mw-content-ltr p");
        StringBuilder plainText = new StringBuilder();
        for(Element e : paragraphs){
            //add plain text
            plainText.append(" ").append(e.text().toLowerCase());

            //get All related articles and add to list
            Elements referencedArticles = e.getElementsByAttribute("href");
            for(Element href: referencedArticles){
                String hrefValue = href.attribute("href").getValue();
                if (!hrefValue.startsWith("#cite") && !hrefValue.startsWith("https://") && !hrefValue.startsWith("/w/index.php?"))
                    nextArticles.add(WIKI_URL_BASE + href.attribute("href").getValue());
            }
        }
        this.text = plainText.toString().replaceAll("\\[(\\d+?)\\]", "");
    }

    public WikiArticle(String link, Map<String, AtomicInteger> BoW) {
        this.link = link;
        this.BoW = BoW;
    }


    public String getLink() {
        return link;
    }

    public String getText() {
        return text;
    }

    public List<String> getNextArticles() {
        return nextArticles;
    }

    public void setBoW(Map<String, AtomicInteger> boW) {
        this. BoW = boW;
    }

    public Map<String, AtomicInteger> getBoW() {
        return BoW;
    }

    public void setTFIDF(Map<String, Double> tfidf) {
        this.tfidf = tfidf;
    }

    public Collection<Double> getTFIDF(){
        return tfidf.values();
    }

    public INDArray getWord2VecSummedVector() {
        return word2VecSummedVector;
    }

    public void setWord2VecSummedVector(INDArray word2VecSummedVector) {
        this.word2VecSummedVector = word2VecSummedVector;
    }
}
