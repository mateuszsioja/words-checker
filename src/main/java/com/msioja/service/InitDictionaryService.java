package com.msioja.service;

import com.msioja.model.EnglishDictionary;
import com.msioja.model.PolishDictionary;
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

    @Autowired
    private PolishDictionary polishDictionary;

    @Autowired
    private EnglishDictionary englishDictionary;

    private static final String PL_DICTIONARY_FILE_NAME = "dictionaries/polish-words.txt";
    private static final String EN_DICTIONARY_FILE_NAME = "dictionaries/english-words.txt";

    @PostConstruct
    public void initDictionaries() throws URISyntaxException {
        polishDictionary.setWords(importWords(PL_DICTIONARY_FILE_NAME));
        englishDictionary.setWords(importWords(EN_DICTIONARY_FILE_NAME));
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