package com.msioja.model;

import java.util.Set;

/** Abstract class for all dictionaries stored in memory. */
public abstract class AbstractDictionary {

    /** Contains set of words for particular dictionary. */
    private Set<String> words;

    /** @return set of words. */
    public Set<String> getWords() {
        return words;
    }

    /** @param words set of words to be set. */
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