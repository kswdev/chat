package net.study.messagesystem;

import net.study.messagesystem.dto.websocket.outbound.WriteMessageRequest;
import net.study.messagesystem.handler.CommandHandler;
import net.study.messagesystem.handler.inbound.ResponseDispatcher;
import net.study.messagesystem.handler.inbound.WebSocketMessageHandler;
import net.study.messagesystem.handler.outbound.WebSocketSender;
import net.study.messagesystem.service.RestApiService;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.service.UserService;
import net.study.messagesystem.service.WebSocketService;
import net.study.messagesystem.util.JsonUtil;
import org.jline.reader.UserInterruptException;

import java.io.IOException;

public class MessageClient {

    public static void main(String[] args) {
        final String REST_BASE_URL = "localhost:8090";
        final String WEBSOCKET_BASE_URL = "localhost:8080";
        final String WEBSOCKET_ENDPOINT = "/ws/v1/message";

        TerminalService terminalService;

        try {
            terminalService = TerminalService.create();
            JsonUtil.setTerminalService(terminalService);
        } catch (IOException e) {
            System.err.println("Failed to create TerminalService: " + e.getMessage());
            return;
        }

        UserService userService = new UserService();
        RestApiService restApiService = new RestApiService(terminalService, REST_BASE_URL);
        WebSocketService webSocketService = createWebSocketService(terminalService, userService, WEBSOCKET_BASE_URL, WEBSOCKET_ENDPOINT);
        CommandHandler commandHandler = new CommandHandler(userService, restApiService, webSocketService, terminalService);

        terminalService.printSystemMessage("'/help' Help for commands. ex) /help");

        while (true) {
            try {
                String input = terminalService.readLine("Enter message: ");
                if (!input.isEmpty() && input.charAt(0) == '/') {
                    String[] parts = input.split(" ", 2);
                    String command = parts[0].substring(1);
                    String argument = parts.length > 1 ? parts[1] : "";

                    if (!commandHandler.process(command, argument)) break;

                } else if (!input.isEmpty() && userService.isInChannel()) {
                    terminalService.printMessage("<me>", input);
                    webSocketService.sendMessage(new WriteMessageRequest(userService.getChannelId(), userService.getUsername(), input));
                }
            } catch (UserInterruptException e) {
                terminalService.flush();
                commandHandler.process("exit", "");
                return;
            } catch (NumberFormatException e) {
                terminalService.printSystemMessage("Invalid input: {}" + e.getMessage());
            }
        }
    }

    private static WebSocketService createWebSocketService(TerminalService terminalService, UserService userService, String BASE_URL, String WEBSOCKET_ENDPOINT) {
        WebSocketSender webSocketSender = new WebSocketSender(terminalService);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(userService, terminalService);
        WebSocketMessageHandler webSocketMessageHandler = new WebSocketMessageHandler(responseDispatcher);

        return new WebSocketService(
                BASE_URL, WEBSOCKET_ENDPOINT,
                userService,
                terminalService,
                webSocketSender,
                webSocketMessageHandler);
    }
}