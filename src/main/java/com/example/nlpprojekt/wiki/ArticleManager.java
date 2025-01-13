package com.example.nlpprojekt.wiki;

import javafx.scene.control.ProgressBar;
import org.jsoup.HttpStatusException;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;

public class ArticleManager {

    private static Map<String, AtomicInteger> documentsBagOfWords = new LinkedHashMap<>();
    private static Map<String, WikiArticle> allArticles = new LinkedHashMap<>();
    private static List<String> stopWords;
    private static StanfordCoreNLP pipeline;
    private static WordVectors wordVectors;

    static{
        try {
            stopWords = Files.readAllLines(Paths.get("/Users/karol/IdeaProjects/NLPprojekt/src/main/resources/stopwords-en.txt"));
            //wordVectors = WordVectorSerializer.loadStaticModel(new File("/Users/karol/IdeaProjects/NLPprojekt/src/main/resources/GoogleNews-vectors-negative300.bin"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        props.setProperty("coref.algorithm", "neural");
        pipeline = new StanfordCoreNLP(props);
    }
    public static int addWikiArticles(String startLink, int levels, int max, ProgressBar progressBar) throws IOException {
        Map<Integer, List<WikiArticle>> articleLevels = new HashMap<>();
        List<WikiArticle> allCurrentArticles = new ArrayList<>();

        List<WikiArticle> firstLevelArticles = new ArrayList<>();
        WikiArticle wiki = new WikiArticle(startLink);
        firstLevelArticles.add(wiki);
        articleLevels.put(1, firstLevelArticles);
        allArticles.put(wiki.getLink(), wiki);
        allCurrentArticles.add(wiki);

        outerloop:
        for(int i=1; i<=levels; i++){
            for(WikiArticle article: articleLevels.get(i)){
                articleLevels.put(i+1, new ArrayList<>());

                for(String articleLink : article.getNextArticles()){
                    progressBar.setProgress(allCurrentArticles.size()/article.getNextArticles().size());
                    WikiArticle wa;
                    try{
                        wa = new WikiArticle(articleLink);
                    } catch (HttpStatusException httpStatusException){
                        System.out.println("Wiki not found: " + articleLink);
                        continue;
                    }

                    if(!allArticles.containsKey(wa.getLink())){
                        allArticles.put(wa.getLink(), wa);
                        allCurrentArticles.add(wa);
                        System.out.println("Wiki  found: " + allArticles.size());
                        articleLevels.get(i+1).add(wa);

                        if(allCurrentArticles.size() >= max){
                            break outerloop;
                        }
                    }
                }
            }
        }

        for(WikiArticle wikiArticle : allCurrentArticles){
            System.out.println("Current article: " + wikiArticle.getLink());
            HashMap<String, AtomicInteger> BoW = parseTextAndGetBOW(wikiArticle.getText());
            wikiArticle.setBoW(BoW);
            addToBagOfWords(BoW.keySet());
            //wikiArticle.setWord2VecSummedVector(getSummedEmbedding(BoW.keySet()));
        }

        return allCurrentArticles.size();
    }

    public static  HashMap<String, AtomicInteger> parseTextAndGetBOW(String text)  {
        HashMap<String, AtomicInteger> BoW = new HashMap<>();
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

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

        return BoW;
    }

    private static void addToBagOfWords(Set<String> dictionary) {
        for(String word : dictionary){
            if (documentsBagOfWords.containsKey(word)) {
                documentsBagOfWords.get(word).incrementAndGet();
            } else {
                documentsBagOfWords.put(word, new AtomicInteger(1));
            }
        }
    }

    private static boolean isWord(CoreLabel l){
        return !l.tag().equals("POS") && !l.tag().equals("SYM") && !l.tag().equals(".") && !l.tag().equals("HYPH") &&
                !l.tag().equals("''") && !l.tag().equals("``") && !l.tag().equals(",") && !l.tag().equals("-RRB-") &&
                !l.tag().equals("-LRB-") && !l.tag().equals(":") && !l.tag().equals("CD") && !l.tag().equals("$") &&
                !l.tag().equals("£") && !l.tag().equals("CC") && !l.tag().equals("NFP") && !l.lemma().contains(",");
    }

    public static void recalculateAllTFIDF() {
        allArticles.values().forEach(article -> article.setTFIDF(calculateTFIDF(article.getBoW())));
    }

    public static Map<String, Double> calculateTFIDF(Map<String, AtomicInteger> inputBoW) {
        Map<String, Double> localTFIDF = new LinkedHashMap<>();
        for(Map.Entry<String,AtomicInteger> entry : documentsBagOfWords.entrySet()){
            String word = entry.getKey();
            double tfidf = 0.0;

            if (inputBoW.containsKey(word)){
                tfidf = inputBoW.get(word).doubleValue() * Math.log((double) allArticles.size() / entry.getValue().doubleValue());
            }
            localTFIDF.put(word, tfidf);
        }

        return localTFIDF;
    }

    public static Map<String, Double> calculateSimilaritiesToInput(String text) {
        Map<String, AtomicInteger> inputBoW = parseTextAndGetBOW(text);

        recalculateAllTFIDF();

        Map<String, Double> inputTFIDF = calculateTFIDF(inputBoW);

        Map<String, Double> result = new HashMap<>();
        for(Map.Entry<String, WikiArticle> articleEntry: allArticles.entrySet()){
            double similarity = calculateCosineSimilarity(inputTFIDF.values(), articleEntry.getValue().getTFIDF());
            result.put(articleEntry.getKey(), similarity);
        }

        return result;
    }

    public static double calculateCosineSimilarity(Collection<Double> setA, Collection<Double> setB) {
        double[] vectorA = setA.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
        double[] vectorB = setB.stream()
                .mapToDouble(Double::doubleValue) // Unbox Double to double
                .toArray();


        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Wektory muszą mieć tę samą długość.");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0 || normB == 0) {
            throw new IllegalArgumentException("Wektory nie mogą być zerowe.");
        }

        return dotProduct / (normA * normB);
    }

    public static void saveVectorsToFiles() throws IOException {
        BufferedWriter dictionaryWriter = new BufferedWriter(new FileWriter("/Users/karol/IdeaProjects/NLPprojekt/src/main/resources/dictionary.txt"));

        for (Map.Entry<String,AtomicInteger> entry : documentsBagOfWords.entrySet()){
            dictionaryWriter.write(entry.getKey() + ":" + entry.getValue());
            dictionaryWriter.newLine();
        }

        BufferedWriter articlesWriter = new BufferedWriter(new FileWriter("/Users/karol/IdeaProjects/NLPprojekt/src/main/resources/article_vectors.txt"));

        for(Map.Entry<String,WikiArticle> article : allArticles.entrySet()){
            articlesWriter.write(article.getKey() + "|||");
            StringBuilder bowLine = new StringBuilder();
            for(Map.Entry<String, AtomicInteger> entry : article.getValue().getBoW().entrySet()){
                bowLine.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
            }

            articlesWriter.write(bowLine.substring(0, bowLine.toString().length()-1));
            articlesWriter.newLine();
        }

        articlesWriter.close();
        dictionaryWriter.close();
    }

    public static void loadVectors() throws IOException {
        BufferedReader dictionaryReader = new BufferedReader(new FileReader("/Users/karol/IdeaProjects/NLPprojekt/src/main/resources/dictionary.txt"));
        String dictionaryLine;

        while ((dictionaryLine = dictionaryReader.readLine()) != null) {
            String word = dictionaryLine.substring(0, dictionaryLine.indexOf(":"));
            AtomicInteger count = new AtomicInteger(Integer.parseInt(dictionaryLine.substring(dictionaryLine.indexOf(":")+1)));
            documentsBagOfWords.put(word, count);
        }
        dictionaryReader.close();

        BufferedReader articlesReader = new BufferedReader(new FileReader("/Users/karol/IdeaProjects/NLPprojekt/src/main/resources/article_vectors.txt"));
        String articlesLine;

        while ((articlesLine = articlesReader.readLine()) != null) {
            HashMap<String, AtomicInteger> BoW = new HashMap<>();
            String link = articlesLine.substring(0, articlesLine.indexOf("|||"));
            List<String> entries = Arrays.stream(articlesLine.substring(articlesLine.indexOf("|||")+3).split(";")).toList();
            for(String entry : entries){
                String[] pair = entry.split(":");
                BoW.put(pair[0],  new AtomicInteger(Integer.parseInt(pair[1])));
            }
            allArticles.put(link, new WikiArticle(link, BoW));
        }
        articlesReader.close();
    }

    public static int getNumberOfArticles(){
        return allArticles.size();
    }

    public static List<Double> getSummedEmbedding(Set<String> words) {
        double[] summedVector = null;

        for (String word : words) {
            if (wordVectors.hasWord(word)) { // Sprawdź, czy słowo istnieje w modelu
                double[] wordVector = wordVectors.getWordVector(word);

                if (summedVector == null) {
                    // Zainicjalizuj sumę jako kopię wektora pierwszego słowa
                    summedVector = new double[wordVector.length];
                }

                // Dodaj bieżący embedding do wektora sumy
                for (int i = 0; i < wordVector.length; i++) {
                    summedVector[i] += wordVector[i];
                }
            } else {
                System.out.println("Słowo nieznane (OOV): " + word);
            }
        }

        return Arrays.stream(summedVector).boxed().collect(Collectors.toList());
    }

}
