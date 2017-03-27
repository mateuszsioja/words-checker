package com.msioja.controller;

import com.msioja.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private LanguageService languageService;

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
}
