package net.study.messagesystem.handler;

import net.study.messagesystem.dto.user.InviteCode;
import net.study.messagesystem.dto.websocket.outbound.FetchUserInviteCodeRequest;
import net.study.messagesystem.dto.websocket.outbound.InviteRequest;
import net.study.messagesystem.service.RestApiService;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.service.WebSocketService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandHandler {

    private final RestApiService restApiService;
    private final WebSocketService webSocketService;
    private final TerminalService terminalService;
    private final Map<String, Function<String[], Boolean>> commands = new HashMap<>();

    public CommandHandler(RestApiService restApiService, WebSocketService webSocketService, TerminalService terminalService) {
        this.restApiService = restApiService;
        this.webSocketService = webSocketService;
        this.terminalService = terminalService;
        prepareCommandMap();
    }

    private void prepareCommandMap() {
        commands.put("register", this::register);
        commands.put("unregister", this::unregister);
        commands.put("login", this::login);
        commands.put("logout", this::logout);
        commands.put("clear", this::clear);
        commands.put("invite", this::invite);
        commands.put("inviteCode", this::inviteCode);
        commands.put("exit", this::exit);
        commands.put("help", this::help);
    }

    public boolean process(String command, String argument) {
        Function<String[], Boolean> commander = commands.getOrDefault(command, (__) -> {
            terminalService.printSystemMessage("Invalid command: %s".formatted(command));
            return true;
        });

        return commander.apply(argument.split(" "));
    }

    private Boolean register(String[] params) {
        if (params.length > 1) {
            if (restApiService.register(params[0], params[1]))
                terminalService.printSystemMessage("Register success.");
            else
                terminalService.printSystemMessage("Register failed.");
        }
        return true;
    }

    private Boolean unregister(String[] params) {
        webSocketService.closeSession();
        if (restApiService.unregister())
            terminalService.printSystemMessage("Unregister success.");
        else
            terminalService.printSystemMessage("Unregister failed.");
        return true;
    }

    private Boolean login(String[] params) {
        if (params.length > 1) {
            if (restApiService.login(params[0], params[1])) {
                if (webSocketService.createSession(restApiService.getSessionId()))
                    terminalService.printSystemMessage("Login success.");
            } else
                terminalService.printSystemMessage("Login failed.");
        }
        return true;
    }

    private Boolean logout(String[] params) {
        if (restApiService.logout()) {
            terminalService.printSystemMessage("Logout success.");
        } else {
            terminalService.printSystemMessage("Logout failed.");
        }

        webSocketService.closeSession();
        return true;
    }

    private Boolean inviteCode(String[] params) {
        webSocketService.sendMessage(new FetchUserInviteCodeRequest());
        terminalService.printSystemMessage("Requesting invite code...");
        return true;
    }

    private Boolean invite(String[] params) {
        if (params.length > 0) {
            webSocketService.sendMessage(new InviteRequest(new InviteCode(params[0])));
            terminalService.printSystemMessage("Request user Invite.");
        }
        return true;
    }

    private Boolean clear(String[] params) {
        terminalService.clearTerminal();
        terminalService.printSystemMessage("Terminal cleared.");
        return true;
    }

    private Boolean exit(String[] params) {
        logout(params);
        terminalService.printSystemMessage("Exiting...");
        return false;
    }

    private Boolean help(String[] params) {
        terminalService.printSystemMessage("""
            Commands:
            '/register' Register a new user. Usage: '/register <Username> <Password>'
            '/unregister' Unregister a user. Usage: '/unregister'
            '/login' Login a user. Usage: '/login <Username> <Password>'
            '/logout' Logout a user. Usage: '/logout'
            '/inviteCode' Get inviteCode: '/inviteCode'
            '/invite' Invite a user: '/invite <InviteCode>'
            '/clear' Clear terminal. Usage: '/clear'
            '/exit' Exit the client. Usage: '/exit'
        """);

        return true;
    }
}
