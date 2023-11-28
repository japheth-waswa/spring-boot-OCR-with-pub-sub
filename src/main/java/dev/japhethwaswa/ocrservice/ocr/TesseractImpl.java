package dev.japhethwaswa.ocrservice.ocr;

public class TesseractImpl {
    public static void main(String[] args) {

        String txt  = new TesseractOcr().extractTextFromImage("./kenya.png");
    }

}
