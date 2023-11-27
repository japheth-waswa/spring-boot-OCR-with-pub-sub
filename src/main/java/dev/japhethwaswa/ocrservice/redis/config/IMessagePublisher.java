package dev.japhethwaswa.ocrservice.redis.config;

public interface IMessagePublisher {
    void publish(String message);
}
