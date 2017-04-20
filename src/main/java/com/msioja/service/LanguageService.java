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

    static final String PL = "LANGUAGE/POLISH";
    private static final String EN = "LANGUAGE/ENGLISH";
    private static final String UNKNOWN = "LANGUAGE/?";

    private static final String IGNORED_CHARS = "[\\-+.\\^:,?!'\"()\\[\\];<>]";
    private static final String SPLIT_WORDS_REGEX = "\\s* \\s*";


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
