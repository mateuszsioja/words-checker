package com.msioja.service;

import com.msioja.model.PunctuationDictionary;
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

import static com.msioja.service.Constants.*;

@Service
public class PunctuationService {

    private final ShortcutsDictionary shortcutsDictionary;
    private final PunctuationDictionary punctuationDictionary;

    private static final String FIRST_LETTER_NOT_UPPERCASE = "Zdanie powinno zaczynać się od wielkiej litery.";
    private static final String LACK_OF_COMA = "Brak przecinka.";

    private List<PunctuationError> abnormalities;

    @Autowired
    public PunctuationService(ShortcutsDictionary shortcutsDictionary, PunctuationDictionary punctuationDictionary) {
        this.shortcutsDictionary = shortcutsDictionary;
        this.punctuationDictionary = punctuationDictionary;
    }

    public List<PunctuationError> checkPunctuation(String stringToCheck) {
        abnormalities = new ArrayList<>();
        List<String> sentences = splitIntoSentences(stringToCheck);
        sentences.forEach(this::checkIfSentenceStartsWithUppercase);
        sentences.forEach(this::checkSingleWords);
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
                Pattern p = Pattern.compile(SPLIT_BY_SPACE_OR_COMMA_REGEX + shortcut);
                Matcher m = p.matcher(split);
                if (m.find()) {
                    checkNext = false;
                    break;
                } else checkNext = true;
            }
        }
    }

    private List<String> splitIntoWords(String text) {
        return Arrays.asList(text.split(SPLIT_WORDS_REGEX));
    }

    private void checkSingleWords(String sentence) {
        List<String> words = splitIntoWords(sentence);

        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);
            String previousWord = words.get(i - 1);
            Character lastCharPreviousWord = (previousWord.charAt(previousWord.length() - 1));
            punctuationDictionary.getWords().forEach(singleWordInDictionary -> {
                boolean isWordMatches = true;
                Pattern p = Pattern.compile(singleWordInDictionary);
                Matcher m = p.matcher(word);
                if (m.find()) {
                    int startIndex = m.start();
                    int endIndex = m.end();
                    String found = m.group();
                    for (int j = endIndex; j < word.length(); j++) {
                        if (Character.isAlphabetic(word.charAt(j))) {
                            isWordMatches = false;
                            break;
                        }
                    }
                    if (isWordMatches) {
                        if (lastCharPreviousWord != ',') {
                            abnormalities.add(new PunctuationError(LACK_OF_COMA, previousWord + " " + word));
                        }
                    }
                }
            });

        }
    }
}
