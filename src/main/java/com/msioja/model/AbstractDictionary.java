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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractDictionary that = (AbstractDictionary) o;

        return words != null ? words.equals(that.words) : that.words == null;
    }

    @Override
    public int hashCode() {
        return words != null ? words.hashCode() : 0;
    }
}