package com.fighter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class InvalidSessionController {

    @RequestMapping(value = "/invalidSession" , method = RequestMethod.GET)
    public String invalidSession() {
        return "shit";
    }
}
