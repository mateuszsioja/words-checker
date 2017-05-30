package com.msioja.service;

import com.msioja.model.PunctuationDictionary;
import com.msioja.model.PunctuationError;
import com.msioja.model.ShortcutsDictionary;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.msioja.service.Constants.*;

/**
 * Class that provides punctuation errors detection.
 */
@Service
public class PunctuationService {

    private final ShortcutsDictionary shortcutsDictionary;
    private final PunctuationDictionary punctuationDictionary;

    private static final String FIRST_LETTER_NOT_UPPERCASE = "Zdanie powinno zaczynać się od wielkiej litery ";
    private static final String LACK_OF_COMMA = "Prawdopodobnie brak przecinka ";
    private static final String REDUNDANT_COMMA = "Prawdopodobnie zbędny przecinek ";
    private static final String LACK_OF_CLOSING_BRACKET = "Niesparowany symbol nawiasu ";

    /** Global list of punctuation errors. */
    private List<PunctuationError> abnormalities;
    private boolean isPunctuationError = true;

    @Autowired
    public PunctuationService(ShortcutsDictionary shortcutsDictionary, PunctuationDictionary punctuationDictionary) {
        this.shortcutsDictionary = shortcutsDictionary;
        this.punctuationDictionary = punctuationDictionary;
    }

    /**
     * Detection of punctuation errors in a given text.
     * Supports polish language.
     *
     * @param stringToCheck Text in which the punctuation will be checked.
     * @return list of detected punctuation errors.
     */
    public List<PunctuationError> checkPunctuation(String stringToCheck) throws IOException {
        abnormalities = new ArrayList<>();
        stringToCheck = stringToCheck.trim();
        List<String> sentences = splitIntoSentences(stringToCheck);
        sentences.forEach(this::checkIfSentenceStartsWithUppercase);
        sentences.forEach(this::checkSingleWordsPunctuation);
        sentences.forEach(this::checkMultipleWordsPunctuation);
        sentences.forEach(this::checkWhetherBracketsArePaired);
        checkUsingTool(stringToCheck);
        return abnormalities;
    }

    /**
     * Split text into words using regular expression.
     *
     * @param text Text to be split into words.
     * @return list of words.
     */
    private List<String> splitIntoWords(String text) {
        return Arrays.asList(text.split(SPLIT_WORDS_REGEX));
    }

    /**
     * Split text into sentences using BreakIterator.
     *
     * @param textToSplit Text to be split into sentences.
     * @return list of sentences.
     */
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

    /**
     * Check if the sentence begins with a uppercase. If not, an error is added.
     */
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

    /**
     * Finds the words in given sentence before which the comma should be.
     * If not, an error is added.
     *
     * For example, in sentence:
     * "Tak lecz nie jestem pewien." - The comma should occur before 'lecz'.
     */
    private void checkSingleWordsPunctuation(String sentence) {
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
                    if (m.start() != 0) {
                        isWordMatches = false;
                    }
                    for (int j = m.end(); j < word.length(); j++) {
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

    /**
     * Finds the exceptional words in given sentence before which we not always put comma
     * if before them occur a specific word.
     * If comma does not occur in right place, an error is added.
     *
     * For example, in sentence:
     * "Pomyślę zwłaszcza że to jutro." - The comma should occur before 'zwłaszcza'.
     * But in sentence:
     * "Pomyślę że to jutro." - The comma should occur before 'że'.
     */
    private void checkMultipleWordsPunctuation(String sentence) {
        List<String> words = splitIntoWords(sentence);
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);
            String previousWord = words.get(i - 1);
            Character lastCharPreviousWord = (previousWord.charAt(previousWord.length() - 1));
            String earlierThanPreviousWord = " ";
            Character lastCharEarlierThanPreviousWord = ' ';
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
                    if (m.start() != 0) {
                        isWordMatches = false;
                    }
                    for (int j = m.end(); j < word.length(); j++) {
                        if (Character.isAlphabetic(word.charAt(j))) {
                            isWordMatches = false;
                            break;
                        }
                    }
                    for (String listWord : listOfWordsInDictionary) {
                        if (previousWord.replaceAll(",", "").equals(listWord) && i > 1) {
                            isMultiple = true;
                        }
                    }

                    if (isWordMatches && !isMultiple) {
                        if (lastCharPreviousWord != ',') {
                            abnormalities.add(new PunctuationError(LACK_OF_COMMA, previousWord + " " + word));
                        }
                    } else if (isWordMatches) {
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

    /**
     * Check whether brackets are properly paired in given sentence.
     * The text is checked character by character and if opening bracket occurs it will be pushed on stack.
     * If closing bracket occurs there will be performed check whether corresponding opening bracket is on the stack.
     * If doesn't, an error will be added.
     */
    private void checkWhetherBracketsArePaired(String sentence) {
        Deque<Character> stack = new ArrayDeque<>();
        for (int i = 0; i < sentence.length(); i++) {
            if (isBracketOpen(sentence.charAt(i))) {
                stack.push(sentence.charAt(i));
            }
            switch (sentence.charAt(i)) {
                case CLOSING_ROUND_BRACKET:
                    checkIfClosingBracketHasPair(OPENING_ROUND_BRACKET, stack.peek() != null ? stack.pop() : 'E');
                    break;
                case CLOSING_SQUARE_BRACKET:
                    checkIfClosingBracketHasPair(OPENING_SQUARE_BRACKET, stack.peek() != null ? stack.pop() : 'E');
                    break;
                case CLOSING_CURLY_BRACKET:
                    checkIfClosingBracketHasPair(OPENING_CURLY_BRACKET, stack.peek() != null ? stack.pop() : 'E');
                    break;
            }
        }
        checkIfUnclosedBracketsExists(stack.size(), stack.peek() != null ? stack.peek() : ' ');
    }

    private boolean isBracketOpen(char letter) {
        return letter == OPENING_ROUND_BRACKET || letter == OPENING_SQUARE_BRACKET || letter == OPENING_CURLY_BRACKET;
    }

    private void checkIfClosingBracketHasPair(char bracket, char bracketOnStack) {
        if (bracket != bracketOnStack || bracketOnStack == 'E') {
            abnormalities.add(new PunctuationError(LACK_OF_CLOSING_BRACKET, String.valueOf(bracket)));
        }
    }

    private void checkIfUnclosedBracketsExists(int stackSize, char higherElementOfStack) {
        if (stackSize > 0) {
            abnormalities.add(new PunctuationError(LACK_OF_CLOSING_BRACKET, String.valueOf(higherElementOfStack)));
        }
    }

    /**
     * Finds punctuation errors using JLanguageTool.
     * If these errors weren't previously detected, they will be added to list of errors.
     */
    private void checkUsingTool(String text) throws IOException {
        JLanguageTool langTool = new JLanguageTool(new Polish());
        List<RuleMatch> matches = langTool.check(text);
        for (RuleMatch match : matches) {
            if (match.getRule().getCategory().getId().toString().equals("PUNCTUATION")) {
                isPunctuationError = true;
                abnormalities.forEach(a -> {
                    String found = match.getSuggestedReplacements().toString();
                    int startIndex = found.indexOf(',') + 2;
                    int endIndex = found.indexOf(' ', startIndex);
                    endIndex = endIndex != -1 ? endIndex : found.length() - 1;
                    found = found.substring(startIndex, endIndex);
                    if (a.getText().contains(found) && !a.getType().equals(FIRST_LETTER_NOT_UPPERCASE)) {
                        isPunctuationError = false;
                    }
                });

                if (isPunctuationError) {
                    abnormalities.add(new PunctuationError("Blad interpunkcyjny ",
                            match.getSuggestedReplacements().toString().replaceAll("[\\[\\]]", "")));
                }
            }
        }
    }

}