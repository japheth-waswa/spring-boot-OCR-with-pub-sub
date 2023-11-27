package dev.japhethwaswa.ocrservice.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Web {

    @GetMapping("/")
    public String root(){
        return "Hello World";
    }
}
