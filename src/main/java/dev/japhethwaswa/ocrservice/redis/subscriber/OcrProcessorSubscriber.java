package dev.japhethwaswa.ocrservice.redis.subscriber;

import com.google.gson.Gson;
import dev.japhethwaswa.ocrservice.api.model.OCRSTATUS;
import dev.japhethwaswa.ocrservice.api.model.Ocr;
import dev.japhethwaswa.ocrservice.ocr.TesseractOcr;
import dev.japhethwaswa.ocrservice.redis.publisher.OcrSuccessPublisher;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.*;


//@Component
public class OcrProcessorSubscriber implements MessageListener {
    private final List<String> messagesList = new ArrayList<>();

    private RedisTemplate<String, Object> redisTemplate;

    public OcrProcessorSubscriber(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate=redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        Gson gson = new Gson();
        Ocr ocr = gson.fromJson(message.toString(), Ocr.class);

        System.out.println("Message Received:- " + ocr);

        //download the file
        FetchFile fetchFile = new FetchFile();
        String filePath = fetchFile.fetchZurimateFile(ocr.fileId(), ocr.fileName());
        if (filePath == null) {
            return;
        }

        // extract extension
        String fileExt = FilenameUtils.getExtension(ocr.fileName());

        Map<Integer, String> fileContent = new HashMap<>();
        TesseractOcr tesseractOcr;
        if (fileExt.equalsIgnoreCase("pdf")) {
            tesseractOcr = new TesseractOcr(false, true);
            Map<Integer, String> pdfContent = tesseractOcr.extractContentFromPdf(filePath, "" + new Random().nextInt(500_000_00, 1_000_000_000));
            if (pdfContent != null && !pdfContent.isEmpty()) fileContent = pdfContent;
        } else {
            tesseractOcr = new TesseractOcr(true, false);
            String imageContent = tesseractOcr.extractTextFromImage(filePath, "" + new Random().nextInt(700_000_00, 1_000_000_000));
            if (imageContent != null) fileContent.put(1, imageContent);
        }
//        System.out.println(fileContent);

        if(fileContent.isEmpty())return;

        //publish to redis channel
        new OcrSuccessPublisher(redisTemplate, new ChannelTopic("OCR-PROCESSOR-STATUS")).publish(gson.toJson(new Ocr(ocr.fileId(), ocr.fileName(),fileContent, OCRSTATUS.SUCCESS)));
        System.out.println("Successfully published");
    }

    public List<String> getMessages() {
        return messagesList;
    }
}
