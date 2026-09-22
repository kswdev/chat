package net.study.messagesocial.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class JsonUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<String> toJson(Object object) {
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (Exception e) {
            log.error("Failed to serialize object: {}", object, e);
            return Optional.empty();
        }
    }
}
