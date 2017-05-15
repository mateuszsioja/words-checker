package com.msioja.service;

import com.msioja.model.PunctuationDictionary;
import com.msioja.model.PunctuationError;
import com.msioja.model.ShortcutsDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.msioja.service.Constants.*;

@Service
public class PunctuationService {

    private final ShortcutsDictionary shortcutsDictionary;
    private final PunctuationDictionary punctuationDictionary;

    private static final String FIRST_LETTER_NOT_UPPERCASE = "Zdanie powinno zaczynać się od wielkiej litery. ";
    private static final String LACK_OF_COMMA = "Brak przecinka. ";
    private static final String REDUNDANT_COMMA = "Przecinek nie powinien tu wystapic. ";

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
        sentences.forEach(this::checkMultipleWords);
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
                            abnormalities.add(new PunctuationError(LACK_OF_COMMA, previousWord + " " + word));
                        }
                    }
                }
            });

        }
    }

    private void checkMultipleWords(String sentence) {
        List<String> words = splitIntoWords(sentence);

        for (int i = 1; i < words.size(); i++) {
            String earlierThanPreviousWord = " ";
            Character lastCharEarlierThanPreviousWord = ' ';
            String word = words.get(i);
            String previousWord = words.get(i - 1);
            Character lastCharPreviousWord = (previousWord.charAt(previousWord.length() - 1));
            if (i > 1) {
                earlierThanPreviousWord = words.get(i - 2);
                lastCharEarlierThanPreviousWord = (earlierThanPreviousWord.charAt(earlierThanPreviousWord.length() - 1));
            }

            for (Map.Entry<String, List<String>> entry : punctuationDictionary.getMultipleWords().entrySet()) {
                String wordInDictionary = entry.getKey();
                List<String> listOfWordsInDictionary = entry.getValue();

                boolean isMultiple = false;
                boolean isWordMatches = true;
                Pattern p = Pattern.compile(wordInDictionary);
                Matcher m = p.matcher(word);
                if (m.find()) {
                    int endIndex = m.end();
                    for (int j = endIndex; j < word.length(); j++) {
                        if (Character.isAlphabetic(word.charAt(j))) {
                            isWordMatches = false;
                            break;
                        }
                    }
                    for (String listWord : listOfWordsInDictionary) {
                        if (previousWord.contains(listWord) && i > 1) {
                            isMultiple = true;
                        }
                    }
                    if (isWordMatches && !isMultiple && i > 1) {
                        if (lastCharPreviousWord != ',') {
                            abnormalities.add(new PunctuationError(LACK_OF_COMMA, previousWord + " " + word));
                        }
                    }
                    else if (isWordMatches && isMultiple) {
                        if (lastCharPreviousWord == ',') {
                            abnormalities.add(new PunctuationError(REDUNDANT_COMMA, previousWord + " " + word));
                        }
                        if (lastCharEarlierThanPreviousWord != ',') {
                            abnormalities.add(new PunctuationError(LACK_OF_COMMA,
                                    earlierThanPreviousWord + " " + previousWord));
                        }
                    }
                }
            }
        }
    }
}
