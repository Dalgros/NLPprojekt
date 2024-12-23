package com.example.nlpprojekt.wiki;

import org.jsoup.HttpStatusException;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WikipediaManager {
    private static Map<String, WikiArticle> allArticles = new HashMap<>();
    private static List<String> stopWords;
    private static StanfordCoreNLP pipeline;

    static{
        try {
            stopWords = Files.readAllLines(Paths.get("/Users/karol/IdeaProjects/NLPprojekt/src/main/resources/stopwords-en.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        props.setProperty("coref.algorithm", "neural");
        pipeline = new StanfordCoreNLP(props);
    }
    public static List<WikiArticle> getWikiArticles(String startLink, int levels, int max) throws IOException {
        Map<Integer, List<WikiArticle>> articleLevels = new HashMap<>();

        List<WikiArticle> firstLevelArticles = new ArrayList<>();
        WikiArticle wiki = new WikiArticle(startLink);
        firstLevelArticles.add(wiki);
        articleLevels.put(1, firstLevelArticles);
        allArticles.put(wiki.getLink(), wiki);

        outerloop:
        for(int i=1; i<=levels; i++){
            for(WikiArticle article: articleLevels.get(i)){
                articleLevels.put(i+1, new ArrayList<>());

                for(String articleLink : article.getNextArticles()){
                    WikiArticle wa;
                    try{
                        wa = new WikiArticle(articleLink);
                    } catch (HttpStatusException httpStatusException){
                        System.out.println("Wiki not found: " + articleLink);
                        continue;
                    }

                    if(!allArticles.containsKey(wa.getLink())){
                        allArticles.put(wa.getLink(), wa);
                        System.out.println("Wiki  found: " + allArticles.size());
                        articleLevels.get(i+1).add(wa);

                        if(allArticles.size() >= max){
                            break outerloop;
                        }
                    }
                }
            }
        }

        List<WikiArticle> articleList = allArticles.values().stream().toList();
        articleList.forEach(wikiArticle -> parseArticle(wikiArticle, stopWords));

        return articleList;

    }

    public static void parseArticle(WikiArticle wikiArticle, List<String> stopWords)  {
        HashMap<String, AtomicInteger> BoW = new HashMap<>();
        CoreDocument document = new CoreDocument(wikiArticle.getText());
        pipeline.annotate(document);

        System.out.println("Cuurent article: " + wikiArticle.getLink());
        for(CoreSentence sentence : document.sentences()) {
            //tokenizacja
            for (CoreLabel label : sentence.tokens()) {
                //lematyzacja + toLowerCase
                String lemma = label.lemma().toLowerCase();
                //normalizacja - usuniecie stop wordow i usuniecie znakow
                if(isWord(label) && !stopWords.contains(lemma)){
                    if (BoW.containsKey(lemma)) {
                        BoW.get(lemma).incrementAndGet();
                    } else {
                        BoW.put(lemma, new AtomicInteger(1));
                    }
                }
            }
        }
        wikiArticle.setBoW(BoW);
    }

    static boolean isWord(CoreLabel l){
        return !l.tag().equals("POS") && !l.tag().equals("SYM") && !l.tag().equals(".") && !l.tag().equals("HYPH") &&
                !l.tag().equals("''") && !l.tag().equals("``") && !l.tag().equals(",") && !l.tag().equals("-RRB-") &&
                !l.tag().equals("-LRB-") && !l.tag().equals(":") && !l.tag().equals("CD") && !l.tag().equals("$") &&
                !l.tag().equals("£") && !l.tag().equals("NN") && !l.tag().equals("CC") && !l.tag().equals("NFP") &&
                !l.tag().equals("NNS") && !l.lemma().contains(",");
    }

}
