package dev.japhethwaswa.ocrservice.redis.subscriber;

import dev.japhethwaswa.ocrservice.OcrService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class FetchFile {

    public  String fetchZurimateFile(String fileId, String fileName) {
       return fetch((OcrService.dotenv != null ? OcrService.dotenv.get("READ_FILE_SEVER_BASE_URL") : "http://localhost:23426/fs/rf") + "?fileId=" + fileId, fileName);
    }

    private  String fetch(String fileURL, String fileName) {
        try {
            String fileExt = FilenameUtils.getExtension(fileName);

            //set up headers
            HttpHeaders headers  = new HttpHeaders();
            headers.add("generic-access-key",(OcrService.dotenv != null ? OcrService.dotenv.get("FILE_SERVER_ACCESS_KEY") : "Is9VUWajIUoCoQ3qLDTkx1tSzV526CaxPi"));

            WebClient client = WebClient.create(fileURL);

            Flux<DataBuffer> flux = client.get().headers(h->h.addAll(headers)).retrieve().bodyToFlux(DataBuffer.class);

            Path dirPath = Paths.get(OcrService.dotenv != null ? OcrService.dotenv.get("FILES_STORE") : "./assets/", "" + new Random().nextInt(20_000_000, 50_000_000));
            //create dir
            if (!Files.exists(dirPath)) Files.createDirectories(dirPath);

            Path destination = Paths.get(dirPath.toString(), new Random().nextInt(20_000_000, 50_000_000) + "." + fileExt);

            DataBufferUtils.write(flux, destination).block();
            return destination.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }
}
