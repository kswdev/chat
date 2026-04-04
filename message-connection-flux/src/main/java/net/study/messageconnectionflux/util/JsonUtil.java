package net.study.messageconnectionflux.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
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

    public <T> Mono<T> fromJson(String json, Class<T> clazz) {
        try {
            return Mono.just(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            printParseError(json);
            return Mono.empty();
        }
    }

    public <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            return objectMapper.readerForListOf(clazz).readValue(json);
        } catch (Exception e) {
            printParseError(json);
            return Collections.emptyList();
        }
    }

    public Optional<String> addValue(String json, String key, String value) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(json);
            node.put(key, value);
            return Optional.of(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static void printParseError(String json) {
        log.error("Failed to parse json: {}", json);
    }
}
