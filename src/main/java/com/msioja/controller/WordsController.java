package com.msioja.controller;

import com.msioja.service.LanguageService;
import com.msioja.service.PunctuationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WordsController {

    private final LanguageService languageService;
    private final PunctuationService punctuationService;

    @Autowired
    public WordsController(LanguageService languageService, PunctuationService punctuationService) {
        this.languageService = languageService;
        this.punctuationService = punctuationService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "home";
    }

    @RequestMapping(value = "/check-language", method = RequestMethod.POST)
    public String checkLanguage(Model model, @RequestParam(value = "string") String wordToCheck) {
        String result = languageService.checkLanguage(wordToCheck);
        model.addAttribute("result", result);
        return "home";
    }

    @RequestMapping(value = "/check-punctuation", method = RequestMethod.POST)
    public String checkPunctuation(Model model, @RequestParam(value = "string") String stringToCheck) {
        List<String> result = punctuationService.checkPunctuation(stringToCheck);
        model.addAttribute("result", result);
        return "home";
    }
}
