package dev.japhethwaswa.ocrservice.api.model;

import java.util.Map;

public record Ocr(String fileId, String fileName, Map<Integer,String>data, OCRSTATUS ocrStatus) {
}
