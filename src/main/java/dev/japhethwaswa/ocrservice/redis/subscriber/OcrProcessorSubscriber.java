package dev.japhethwaswa.ocrservice.redis.subscriber;

import com.google.gson.Gson;
import dev.japhethwaswa.ocrservice.api.model.Ocr;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.util.ArrayList;
import java.util.List;

public class OcrProcessorSubscriber implements MessageListener {
    private final List<String> messagesList = new ArrayList<>();
    @Override
    public void onMessage(Message message, byte[] pattern) {
        Gson gson = new Gson();
        Ocr ocr = gson.fromJson(message.toString(),Ocr.class);

        System.out.println(ocr);
        System.out.println(ocr.fileId());
        System.out.println(ocr.ocrStatus());
        System.out.println("Message Received:- " + ocr);
    }

    public List<String> getMessages(){
        return messagesList;
    }
}
