package com.msioja.service;

import com.msioja.model.EnglishDictionary;
import com.msioja.model.PolishDictionary;
import com.msioja.model.PunctuationDictionary;
import com.msioja.model.ShortcutsDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class InitDictionaryService {

    private PolishDictionary polishDictionary;
    private EnglishDictionary englishDictionary;
    private ShortcutsDictionary shortcutsDictionary;
    private PunctuationDictionary punctuationDictionary;

    private static final String PL_DICTIONARY_FILE_NAME = "dictionaries/polish-words.txt";
    private static final String EN_DICTIONARY_FILE_NAME = "dictionaries/english-words.txt";
    private static final String SHORTCUTS_DICTIONARY_FILE_NAME = "dictionaries/shortcuts-words.txt";
    private static final String PUNCTUATION_DICTIONARY_FILE_NAME = "dictionaries/punctuation-words.txt";

    @Autowired
    public InitDictionaryService(PolishDictionary polishDictionary,
                                 EnglishDictionary englishDictionary,
                                 ShortcutsDictionary shortcutsDictionary,
                                 PunctuationDictionary punctuationDictionary) {
        this.polishDictionary = polishDictionary;
        this.englishDictionary = englishDictionary;
        this.shortcutsDictionary = shortcutsDictionary;
        this.punctuationDictionary = punctuationDictionary;
    }

    @PostConstruct
    public void initDictionaries() throws URISyntaxException {
        polishDictionary.setWords(importWords(PL_DICTIONARY_FILE_NAME));
        englishDictionary.setWords(importWords(EN_DICTIONARY_FILE_NAME));
        shortcutsDictionary.setWords(importWords(SHORTCUTS_DICTIONARY_FILE_NAME));
        punctuationDictionary.setWords(importWords(PUNCTUATION_DICTIONARY_FILE_NAME));
    }

    private HashSet<String> importWords(String fileName) throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        Path file = Paths.get(classLoader.getResource(fileName).toURI());
        try {
            return Files.readAllLines(file).stream()
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}