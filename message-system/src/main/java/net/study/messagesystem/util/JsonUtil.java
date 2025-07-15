package net.study.messagesystem.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public Optional<String> toJson(Object object) {
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (Exception e) {
            printParseError(e.getMessage());
            return Optional.empty();
        }
    }

    public <T> Optional<T> fromJson(String json, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            printParseError(json);
            return Optional.empty();
        }
    }

    private static void printParseError(String json) {
        log.error("Failed to parse json: {}", json);
    }
}
