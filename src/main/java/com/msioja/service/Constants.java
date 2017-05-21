package com.msioja.service;

abstract class Constants {
    static final String IGNORED_CHARS = "[\\-+.\\^:,?!'\"()\\[\\];<>]";
    static final String SPLIT_WORDS_REGEX = "\\s* \\s*";
    static final String SPLIT_SENTENCE_BY_DOT_REGEX = "\\s*\\.\\s";
    static final String SPLIT_BY_SPACE_OR_COMMA_REGEX = "[\\s,]";

    static final char OPENING_ROUND_BRACKET = '(';
    static final char CLOSING_ROUND_BRACKET = ')';
    static final char OPENING_SQUARE_BRACKET = '[';
    static final char CLOSING_SQUARE_BRACKET = ']';
    static final char OPENING_CURLY_BRACKET = '{';
    static final char CLOSING_CURLY_BRACKET = '}';
}
