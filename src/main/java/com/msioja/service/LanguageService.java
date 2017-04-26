package com.msioja.service;

import com.msioja.model.EnglishDictionary;
import com.msioja.model.PolishDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.msioja.service.Constants.IGNORED_CHARS;
import static com.msioja.service.Constants.SPLIT_WORDS_REGEX;

@Service
public class LanguageService {

    private final PolishDictionary polishDictionary;
    private final EnglishDictionary englishDictionary;

    private static final String PL = "Wykryto jezyk polski.";
    private static final String EN = "Wykryto jezyk angielski.";
    private static final String UNKNOWN = "Nie rozpoznano jezyka.";

    @Autowired
    public LanguageService(PolishDictionary polishDictionary, EnglishDictionary englishDictionary) {
        this.polishDictionary = polishDictionary;
        this.englishDictionary = englishDictionary;
    }

    public String checkLanguage(String textToCheck) {
        int polishWordsCounter = 0, englishWordsCounter = 0;

        textToCheck = textToCheck.replaceAll(IGNORED_CHARS, "").trim().toLowerCase();
        List<String> wordsToCheck = Arrays.asList(textToCheck.split(SPLIT_WORDS_REGEX));
        List<String> resultOfWordChecks = wordsToCheck.stream()
                .map(this::checkWordLanguage).collect(Collectors.toList());

        for (String word : resultOfWordChecks) {
            if (word.equals(PL)) polishWordsCounter++;
            else if (word.equals(EN)) englishWordsCounter++;
        }

        if (polishWordsCounter == 0 && englishWordsCounter == 0)
            return UNKNOWN;
        return polishWordsCounter >= englishWordsCounter ? PL : EN;
    }

    private String checkWordLanguage(String wordToCheck) {

        if (englishDictionary.getWords().contains(wordToCheck))
            return EN;

        if (polishDictionary.getWords().contains(wordToCheck))
            return PL;

        return UNKNOWN;
    }
}
