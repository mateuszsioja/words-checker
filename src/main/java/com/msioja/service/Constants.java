package com.msioja.service;

abstract class Constants {
    static final String IGNORED_CHARS = "[\\-+.\\^:,?!'\"()\\[\\];<>]";
    static final String SPLIT_WORDS_REGEX = "\\s* \\s*";
    static final String SPLIT_SENTENCE_BY_DOT_REGEX = "\\s*\\.\\s";
    static final String SPLIT_BY_SPACE_OR_COMMA_REGEX = "[\\s,]";
}
