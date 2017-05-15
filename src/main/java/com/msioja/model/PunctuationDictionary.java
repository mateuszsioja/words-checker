package com.msioja.model;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PunctuationDictionary extends AbstractDictionary {
    private Map<String, List<String>> multipleWords;

    public Map<String, List<String>> getMultipleWords() {
        return multipleWords;
    }

    public void setMultipleWords(Map<String, List<String>> multipleWords) {
        this.multipleWords = multipleWords;
    }
}
