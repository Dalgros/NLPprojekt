package com.example.nlpprojekt.wiki;

import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikipediaManager {

    public static List<WikiArticle> getWikiArticles(String startLink, int levels, int max) throws IOException {
        Map<String, WikiArticle> allArticles = new HashMap<>();
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
                        articleLevels.get(i+1).add(wa);

                        if(allArticles.size() >= max){
                            break outerloop;
                        }
                    }
                }
            }
        }

        return allArticles.values().stream().toList();
    }

}
