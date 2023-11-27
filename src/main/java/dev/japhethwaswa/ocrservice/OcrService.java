package dev.japhethwaswa.ocrservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OcrService {

	public static Dotenv dotenv;
	public static void main(String[] args) {
		dotenv = Dotenv.configure().load();
		SpringApplication.run(OcrService.class, args);
	}

}
