package net.study.messagesystem.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.study.messagesystem.service.TerminalService;

import java.util.Optional;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static TerminalService terminalService;

    private static void setTerminalService(TerminalService terminalService) {
        JsonUtil.terminalService = terminalService;
    }

    public static Optional<String> toJson(Object object) {
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (Exception e) {
            terminalService.printSystemMessage("Failed to parse json: " + object);
            return Optional.empty();
        }
    }

    public static <T> Optional<T> fromJson(String json, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            terminalService.printSystemMessage("Failed to parse json: " + json);
            return Optional.empty();
        }
    }
}
