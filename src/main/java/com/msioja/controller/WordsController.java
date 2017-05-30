package com.msioja.controller;

import com.msioja.service.LanguageService;
import com.msioja.service.PunctuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.logging.Logger;

@Controller
public class WordsController {

    private final Logger log = Logger.getLogger(getClass().getName());
    private final LanguageService languageService;
    private final PunctuationService punctuationService;

    @Autowired
    public WordsController(LanguageService languageService, PunctuationService punctuationService) {
        this.languageService = languageService;
        this.punctuationService = punctuationService;
    }


    /**
     * Returns view of home page.
     *
     * @param model content of previous language and punctuation text
     *              should not be null so an empty string is passed.
     * @return home page.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        model.addAttribute("previousLanguageText", "");
        model.addAttribute("previousPunctuationText", "");
        return "home";
    }

    /**
     * Recognize language in given text and calculate time of response.
     * Supports polish and english language.
     *
     * @param model result of language recognition, time of response.
     * @param stringToCheck text in which the language is to be checked.
     * @return home page with recognized language and time of response.
     */
    @RequestMapping(value = "/check-language", method = RequestMethod.POST)
    public String checkLanguage(Model model, @RequestParam(value = "string") String stringToCheck) {
        long startTime = System.currentTimeMillis();
        log.info("Language check started in text with " + stringToCheck.length() + " characters.");
        model.addAttribute("result", languageService.checkLanguage(stringToCheck));
        model.addAttribute("previousLanguageText", stringToCheck);
        model.addAttribute("previousPunctuationText", "");
        String timeElapsed = String.valueOf(System.currentTimeMillis() - startTime);
        log.info("Time elapsed: " + timeElapsed + " ms.");
        model.addAttribute("time1", "Czas odpowiedzi: " + timeElapsed + " ms.");
        return "home";
    }

    /**
     * Finds punctuation errors in given text and calculate time of response.
     * Supports polish language.
     *
     * @param model result of punctuation errors, time of response.
     * @param stringToCheck text in which the punctuation errors are to be checked.
     * @return home page with found punctuation errors and time of response.
     * @throws IOException thrown by Language Tool.
     */
    @RequestMapping(value = "/check-punctuation", method = RequestMethod.POST)
    public String checkPunctuation(Model model, @RequestParam(value = "string") String stringToCheck) throws IOException {
        long startTime = System.currentTimeMillis();
        log.info("Punctuation check started in text with " + stringToCheck.length() + " characters.");
        model.addAttribute("result2", punctuationService.checkPunctuation(stringToCheck));
        model.addAttribute("previousPunctuationText", stringToCheck);
        model.addAttribute("previousLanguageText", "");
        String timeElapsed = String.valueOf(System.currentTimeMillis() - startTime);
        log.info("Time elapsed: " + timeElapsed + " ms.");
        model.addAttribute("time2", "Czas odpowiedzi: " + timeElapsed + " ms.");
        return "home";
    }
}