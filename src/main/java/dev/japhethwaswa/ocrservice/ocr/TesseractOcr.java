package dev.japhethwaswa.ocrservice.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;


public class TesseractOcr {

    public  String extractTextFromImage(String uri){
//        File tmpFolder= LoadLibs.extractNativeResources("libtesseract");
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("./tessdata");
        String result = null;
        try {
//            result = tesseract.doOCR(new File("/Users/japhethelijah/Downloads/Screenshot 2023-11-28 at 23.55.10.png"));
//            result = tesseract.doOCR(new File("/Users/japhethelijah/Downloads/Screenshot 2023-11-29 at 13.42.03.png"));
//            result = tesseract.doOCR(new File("/Users/japhethelijah/Downloads/Screenshot 2023-11-29 at 13.43.42.png"));
            result = tesseract.doOCR(new File(uri));
//            result = tesseract.doOCR(new File(Objects.requireNonNull(this.getClass().getResource("/static/kenya.png")).toURI()));
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
        System.out.println("-".repeat(50));
        System.out.println(result);
        return result;
    }
}
