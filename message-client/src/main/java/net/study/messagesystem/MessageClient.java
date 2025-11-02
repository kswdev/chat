package net.study.messagesystem;

import net.study.messagesystem.dto.websocket.outbound.WriteMessage;
import net.study.messagesystem.handler.CommandHandler;
import net.study.messagesystem.handler.inbound.ResponseDispatcher;
import net.study.messagesystem.handler.inbound.WebSocketMessageHandler;
import net.study.messagesystem.service.*;
import net.study.messagesystem.util.JsonUtil;
import org.jline.reader.UserInterruptException;

import java.io.IOException;

public class MessageClient {

    public static void main(String[] args) {
        final String BASE_URL = "localhost:80";
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
        RestApiService restApiService = new RestApiService(terminalService, BASE_URL);
        WebSocketService webSocketService = createWebSocketService(terminalService, userService, BASE_URL, WEBSOCKET_ENDPOINT);
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
                    webSocketService.sendMessage(new WriteMessage(userService.getChannelId(), input, System.currentTimeMillis()));
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
        MessageService messageService = new MessageService(userService, terminalService);
        ResponseDispatcher responseDispatcher = new ResponseDispatcher(userService, terminalService, messageService);
        WebSocketMessageHandler webSocketMessageHandler = new WebSocketMessageHandler(responseDispatcher);

        WebSocketService webSocketService = new WebSocketService(
                BASE_URL, WEBSOCKET_ENDPOINT,
                userService,
                terminalService,
                messageService,
                webSocketMessageHandler);

        messageService.setWebSocketService(webSocketService);
        return webSocketService;
    }
}