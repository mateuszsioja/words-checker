package com.msioja.model;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Class to store polish words before which we should put commas.
 */
@Component
public class PunctuationDictionary extends AbstractDictionary {

    /**
     * Map of exceptional words before which we not always put commas
     * if before them occur a specific word.
     */
    private Map<String, List<String>> multipleWords;

    /** @return map of exceptional words. */
    public Map<String, List<String>> getMultipleWords() {
        return multipleWords;
    }

    /** @param multipleWords map of exceptional words to be set. */
    public void setMultipleWords(Map<String, List<String>> multipleWords) {
        this.multipleWords = multipleWords;
    }
}
