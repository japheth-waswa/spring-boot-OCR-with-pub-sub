package dev.japhethwaswa.ocrservice.api.controller;

import com.google.gson.Gson;
import dev.japhethwaswa.ocrservice.api.model.OCRSTATUS;
import dev.japhethwaswa.ocrservice.api.model.Ocr;
import dev.japhethwaswa.ocrservice.redis.publisher.OcrSuccessPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Web {

    @Autowired
    private OcrSuccessPublisher ocrSuccessPublisher;

    @GetMapping("/")
    public String root(){
        return "Hello World";
    }

    @GetMapping("/pub")
    public void publish(){
        Gson gson  = new Gson();
        ocrSuccessPublisher.publish(gson.toJson(new Ocr("89923", OCRSTATUS.PENDING)));
    }
}
