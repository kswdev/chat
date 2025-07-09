package net.study.messagesystem;

import net.study.messagesystem.dto.domain.Message;
import net.study.messagesystem.handler.WebSocketMessageHandler;
import net.study.messagesystem.handler.WebSocketSender;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.service.WebSocketService;

import java.io.IOException;

public class MessageClient {

    public static void main(String[] args) {
        final String BASE_URL = "60.196.157.197:8080";
        final String WEBSOCKET_ENDPOINT = "/ws/v1/message";

        TerminalService terminalService;

        try {
            terminalService = TerminalService.create();
        } catch (IOException e) {
            System.err.println("Failed to create TerminalService: " + e.getMessage());
            return;
        }

        WebSocketSender webSocketSender = new WebSocketSender(terminalService);
        WebSocketService webSocketService = new WebSocketService(terminalService, webSocketSender, BASE_URL, WEBSOCKET_ENDPOINT);
        webSocketService.setWebSocketMessageHandler(new WebSocketMessageHandler(terminalService));

        while (true) {
            String input = terminalService.readLine("Enter message: ");
            if (!input.isEmpty() && input.charAt(0) == '/') {
                String command = input.substring(1);

                boolean exit = switch (command) {
                    case "exit" -> {
                        webSocketService.closeSession();
                        yield true;
                    }
                    case "clear" -> {
                        terminalService.clearTerminal();
                        yield true;
                    }
                    case "connect" -> {
                        webSocketService.createSession();
                        yield false;
                    }
                    default -> false;
                };

                if (exit) break;

            } else if (!input.isEmpty()) {
                terminalService.printMessage("<me>", input);
                webSocketService.sendMessage(new Message("test client", input));
            }
        }
    }
}