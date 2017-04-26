package com.msioja.service;

import com.msioja.model.PunctuationError;
import com.msioja.model.ShortcutsDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.msioja.service.Constants.SPLIT_SENTENCE_BY_DOT_REGEX;

@Service
public class PunctuationService {

    private final ShortcutsDictionary shortcutsDictionary;

    private static final String FIRST_LETTER_NOT_UPPERCASE = "Zdanie powinno zaczynać się od wielkiej litery.";
    private static final String LACK_OF_SPACE_AFTER_COMMA = "Po przecinku stawiamy spacje";

    private List<PunctuationError> abnormalities;

    @Autowired
    public PunctuationService(ShortcutsDictionary shortcutsDictionary) {
        this.shortcutsDictionary = shortcutsDictionary;
    }

    public List<PunctuationError> checkPunctuation(String stringToCheck) {
        abnormalities = new ArrayList<>();
        List<String> sentences = splitIntoSentences(stringToCheck);
        sentences.forEach(this::checkIfSentenceStartsWithUppercase);
        return abnormalities;
    }

    private List<String> splitIntoSentences(String textToSplit) {
        List<String> sentences = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(textToSplit);
        int start = iterator.first();
        for (int end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()) {
            sentences.add(textToSplit.substring(start, end));
        }
        return sentences;
    }

    private void checkIfSentenceStartsWithUppercase(String sentence) {
        List<String> splitSentenceByDot = Arrays.asList(sentence.split(SPLIT_SENTENCE_BY_DOT_REGEX));
        boolean checkNext = true;

        for (String split : splitSentenceByDot) {
            if (Character.isLowerCase(split.charAt(0)) && checkNext) {
                abnormalities.add(new PunctuationError(FIRST_LETTER_NOT_UPPERCASE, split));
            }
            for (String shortcut : shortcutsDictionary.getWords()) {
                Pattern p = Pattern.compile("[\\s,]" + shortcut);
                Matcher m = p.matcher(split);
                if (m.find()) {
                    checkNext = false;
                    break;
                } else checkNext = true;
            }
        }
    }
}
