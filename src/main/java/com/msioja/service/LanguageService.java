package com.msioja.service;

import com.msioja.model.EnglishDictionary;
import com.msioja.model.PolishDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LanguageService {

    @Autowired
    private PolishDictionary polishDictionary;

    @Autowired
    private EnglishDictionary englishDictionary;

    private static final String PL = "LANGUAGE/POLISH";
    private static final String EN = "LANGUAGE/ENGLISH";
    private static final String UNKNOWN = "LANGUAGE/?";


    public String checkLanguage(String textToCheck) {
        int polishWordsCounter = 0, englishWordsCounter = 0;

        textToCheck = textToCheck.replaceAll("[\\-+.\\^:,?!'\"()\\[\\];<>]", "").trim().toLowerCase();
        List<String> wordsToCheck = Arrays.asList(textToCheck.split("\\s* \\s*"));
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
