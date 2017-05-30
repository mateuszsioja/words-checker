package com.msioja.model;

/**
 *  Class which represents punctuation error.
 */
public class PunctuationError {

    /** Type of punctuation error. */
    private String type;

    /** Snippet of the text affected by punctuation error. */
    private String text;

    public PunctuationError(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
