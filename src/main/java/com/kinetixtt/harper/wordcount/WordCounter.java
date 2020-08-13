package com.kinetixtt.harper.wordcount;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Class is threadsafe.
 * <p>
 * TODO: confirm assumptions:
 * 1. words are considered case insensitive
 * 2. null and empty string is not considered valid words
 * 3. some caching of latest words will be implemented at Translator level down the line
 */
@Component
public class WordCounter {
    private static final String EMPTY_STRING = "";

    private final ConcurrentMap<String, Long> wordCount = Maps.newConcurrentMap();

    private final Translator translator;

    @Autowired
    public WordCounter(Translator translator) {
        this.translator = translator;
    }

    public void add(final String... words) {
        if (words == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        List<String> wordsAsList = Arrays.asList(words.clone());
        if (wordsAsList.isEmpty()) {
            throw new IllegalArgumentException("Specify one or more words");
        } else if (wordsAsList.contains(null) || wordsAsList.contains(EMPTY_STRING)) {
            throw new IllegalArgumentException("Specified parameters must be valid words");
        } else if (wordsAsList.stream().anyMatch(word -> word.chars().anyMatch(ch -> !Character.isLetter(ch)))) {
            throw new IllegalArgumentException("Specified words must only contain alphabetic characters");
        }

        // convert to word by count upfront so we spend less time locking map in next step (fewer words to loop through)
        Map<String, Long> providedWordCount = wordsAsList.stream().map(word -> translator.translate(word.toLowerCase())).collect(Collectors.groupingBy(k -> k, Collectors.counting()));

        // update count in threadsafe manner with minimal blocking
        providedWordCount.entrySet().stream().forEach(word -> wordCount.merge(word.getKey(), word.getValue(), (prev, current) -> prev + current));
    }

    public long countOfWord(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Must specify word for finding count");
        }
        return wordCount.getOrDefault(translator.translate(word.toLowerCase()), 0L);
    }
}
