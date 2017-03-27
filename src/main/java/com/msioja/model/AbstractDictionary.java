package com.msioja.model;

import java.util.Set;

public abstract class AbstractDictionary {

    private Set<String> words;

    public Set<String> getWords() {
        return words;
    }

    public void setWords(Set<String> words) {
        this.words = words;
    }
}