package com.msioja.service;

import com.msioja.model.PolishDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class PunctuationService {

    @Autowired
    private LanguageService languageService;

    private static final String LANGUAGE_NOT_SUPPORT = "Błędy interpunkcyjne nie są sprawdzane dla tekstu w podanym języku.";
    private static final String FIRST_LETTER_NOT_UPPERCASE = "Zdanie powinno zaczynać się od wilekiej litery.";

    private String abnormalities = "";

    public String checkPunctuation(String stringToCheck) {
        String language = languageService.checkLanguage(stringToCheck);
        if(!language.equals(LanguageService.PL)) {
            return LANGUAGE_NOT_SUPPORT;
        }

        return abnormalities;
    }

    private List<String> splitIntoSentencesAndCheck(String textToSplit) {
        List<String> wordsToCheck = Arrays.asList(textToSplit.split("."));
        wordsToCheck.forEach(sentence -> {
            if (Character.isUpperCase(sentence.charAt(0))) {
                abnormalities = abnormalities.concat(FIRST_LETTER_NOT_UPPERCASE);
            }
        });
        return null;
    }
}
