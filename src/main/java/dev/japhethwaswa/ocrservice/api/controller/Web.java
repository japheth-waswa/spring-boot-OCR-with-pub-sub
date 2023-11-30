package dev.japhethwaswa.ocrservice.api.controller;

import com.google.gson.Gson;
import dev.japhethwaswa.ocrservice.api.model.OCRSTATUS;
import dev.japhethwaswa.ocrservice.api.model.Ocr;
import dev.japhethwaswa.ocrservice.ocr.TesseractOcr;
import dev.japhethwaswa.ocrservice.redis.publisher.OcrSuccessPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
public class Web {

    @Autowired
    private OcrSuccessPublisher ocrSuccessPublisher;

    @GetMapping("/img")
    public String getImgText(){

        String txt  = new TesseractOcr().extractTextFromImage("./assets/kenya.png","" + new Random().nextInt(500_000_00, 1_000_000_000));
        System.out.println(txt);
        return "Done IMG";
    }
    @GetMapping("/pdf")
    public String getPdfText(){

        Map<Integer, String> pdfData = new TesseractOcr(false,true).extractContentFromPdf("./assets/DoD.pdf"
                , "" + new Random().nextInt(500_000_00, 1_000_000_000));
        System.out.println(pdfData);
        return "Done PDF";
    }

    @GetMapping("/pub")
    public void publish(){
        Gson gson  = new Gson();
        ocrSuccessPublisher.publish(gson.toJson(new Ocr("65675ee2b3f11803c23c6658","BPM SOLUTION LEGAL SERVICES.pdf.pdf",null, OCRSTATUS.PENDING)));
    }

}
