package com.msioja.service;

import com.msioja.model.PolishDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class PunctuationService {

    @Autowired
    private LanguageService languageService;

    private static final String SPLIT_SENTENCES_REGEX = ".|\\?|!";

    private static final String LANGUAGE_NOT_SUPPORT = "Błędy interpunkcyjne nie są sprawdzane dla tekstu w podanym języku.";
    private static final String FIRST_LETTER_NOT_UPPERCASE = "Zdanie powinno zaczynać się od wilekiej litery.";

    private List<String> abnormalities;

    public List<String> checkPunctuation(String stringToCheck) {
        abnormalities = new ArrayList<>();
        String language = languageService.checkLanguage(stringToCheck);
//        if(!language.equals(LanguageService.PL)) {
//            abnormalities.add(LANGUAGE_NOT_SUPPORT);
//            return abnormalities;
//        }
        splitIntoSentencesAndCheck(stringToCheck);
        return abnormalities;
    }

    private List<String> splitIntoSentencesAndCheck(String textToSplit) {
        List<String> sentencesToCheck = new ArrayList<>();

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        String source = "This is a test. This is a T.L.A. test. Now with a Dr. in it.";
        iterator.setText(textToSplit);
        int start = iterator.first();
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            sentencesToCheck.add(textToSplit.substring(start,end));
        }

        sentencesToCheck.forEach(sentence -> {
            if (Character.isLowerCase(sentence.charAt(0))) {
                abnormalities.add(FIRST_LETTER_NOT_UPPERCASE);
            }
        });
        return null;
    }
}
