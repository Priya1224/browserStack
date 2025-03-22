package com.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Words {
    public static Map<String, Integer> countRepeatedWords(List<String> titles) {
        Map<String, Integer> wordCounts = new HashMap<>();

        for (String title : titles) {
            // Convert title to lowercase and split by non-word characters
            String[] words = title.toLowerCase().split("\\W+");

            for (String word : words) {
                if (!word.isEmpty()) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }

        return wordCounts;
    }
}
