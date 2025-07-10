package net.study.messagesystem;

import net.study.messagesystem.dto.websocket.inbound.MessageRequest;
import net.study.messagesystem.handler.CommandHandler;
import net.study.messagesystem.handler.WebSocketMessageHandler;
import net.study.messagesystem.handler.WebSocketSender;
import net.study.messagesystem.service.RestApiService;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.service.WebSocketService;

import java.io.IOException;

public class MessageClient {

    public static void main(String[] args) {
        final String BASE_URL = "localhost:8080";
        final String WEBSOCKET_ENDPOINT = "/ws/v1/message";

        TerminalService terminalService;

        try {
            terminalService = TerminalService.create();
        } catch (IOException e) {
            System.err.println("Failed to create TerminalService: " + e.getMessage());
            return;
        }

        WebSocketMessageHandler webSocketMessageHandler = new WebSocketMessageHandler(terminalService);
        WebSocketSender webSocketSender = new WebSocketSender(terminalService);
        WebSocketService webSocketService = new WebSocketService(
                webSocketMessageHandler,
                terminalService,
                webSocketSender,
                BASE_URL, WEBSOCKET_ENDPOINT
        );

        RestApiService restApiService = new RestApiService(terminalService, BASE_URL);
        CommandHandler commandHandler = new CommandHandler(restApiService, webSocketService, terminalService);

        while (true) {
            String input = terminalService.readLine("Enter message: ");
            if (!input.isEmpty() && input.charAt(0) == '/') {
                String[] parts = input.split(" ", 2);
                String command = parts[0].substring(1);
                String argument = parts.length > 1 ? parts[1] : "";

                if (!commandHandler.process(command, argument)) break;

            } else if (!input.isEmpty()) {
                terminalService.printMessage("<me>", input);
                webSocketService.sendMessage(new MessageRequest("test client", input));
            }
        }
    }
}