package net.study.messagesystem.handler;

import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.user.InviteCode;
import net.study.messagesystem.dto.websocket.outbound.*;
import net.study.messagesystem.service.RestApiService;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.service.UserService;
import net.study.messagesystem.service.WebSocketService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CommandHandler {

    private final UserService userService;
    private final RestApiService restApiService;
    private final WebSocketService webSocketService;
    private final TerminalService terminalService;
    private final Map<String, Function<String[], Boolean>> commands = new HashMap<>();

    public CommandHandler(UserService userService, RestApiService restApiService, WebSocketService webSocketService, TerminalService terminalService) {
        this.userService = userService;
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
        commands.put("accept", this::accept);
        commands.put("create", this::create);
        commands.put("join", this::join);
        commands.put("enter", this::enter);
        commands.put("leave", this::leave);
        commands.put("reject", this::reject);
        commands.put("disconnect", this::disconnect);
        commands.put("connections", this::connections);
        commands.put("channels", this::channels);
        commands.put("pending", this::pending);
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
        if (userService.isInLobby() && params.length > 1) {
            if (restApiService.register(params[0], params[1]))
                terminalService.printSystemMessage("Register success.");
            else
                terminalService.printSystemMessage("Register failed.");
        }
        return true;
    }

    private Boolean unregister(String[] params) {
        if (userService.isInLobby()) {
            webSocketService.closeSession();
            if (restApiService.unregister())
                terminalService.printSystemMessage("Unregister success.");
            else
                terminalService.printSystemMessage("Unregister failed.");
        }
        return true;
    }

    private Boolean login(String[] params) {
        if (userService.isInLobby() && params.length > 1) {
            if (restApiService.login(params[0], params[1])) {
                if (webSocketService.createSession(restApiService.getSessionId())) {
                    userService.login(params[0]);
                    terminalService.printSystemMessage("Login success.");
                }
            } else
                terminalService.printSystemMessage("Login failed.");
        }
        return true;
    }

    private Boolean logout(String[] params) {
        if (restApiService.logout()) {
            userService.logout();
            terminalService.printSystemMessage("Logout success.");
        } else {
            terminalService.printSystemMessage("Logout failed.");
        }

        webSocketService.closeSession();
        return true;
    }

    private Boolean inviteCode(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            if ("user".equals(params[0])) {
                webSocketService.sendMessage(new FetchUserInviteCodeRequest());
                terminalService.printSystemMessage("Get invite code.");
            } else if ("channel".equals(params[0])) {
                webSocketService.sendMessage(new FetchChannelInviteCodeRequest(new ChannelId(Long.valueOf(params[1]))));
                terminalService.printSystemMessage("Get invite code of channel.");
            }
        }
        return true;
    }

    private Boolean connections(String[] params) {
        if (userService.isInLobby()) {
            webSocketService.sendMessage(new FetchUserConnectionsRequest(UserConnectionStatus.ACCEPTED));
            terminalService.printSystemMessage("Get connections list");
        }
        return true;
    }

    private Boolean pending(String[] params) {
        if (userService.isInLobby()) {
            webSocketService.sendMessage(new FetchUserConnectionsRequest(UserConnectionStatus.PENDING));
            terminalService.printSystemMessage("Get pending list");
        }
        return true;
    }

    private Boolean invite(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new InviteRequest(new InviteCode(params[0])));
            terminalService.printSystemMessage("Request user Invite.");
        }
        return true;
    }

    private Boolean accept(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new AcceptRequest(params[0]));
            terminalService.printSystemMessage("Accept invite from %s".formatted(params[0]));
        }
        return true;
    }

    private Boolean create(String[] params) {
        if (userService.isInLobby() && 1 < params.length && params.length < 100) {
            webSocketService.sendMessage(new CreateRequest(params[0], List.of(Arrays.copyOfRange(params, 1, params.length))));
            terminalService.printSystemMessage("Request create a direct channel.");
        }
        return true;
    }

    private Boolean join(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new JoinRequest(new InviteCode(params[0])));
            terminalService.printSystemMessage("Request join a channel.");
        }
        return true;
    }

    private Boolean enter(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            ChannelId channelId = new ChannelId(Long.valueOf(params[0]));
            webSocketService.sendMessage(new EnterRequest(channelId));
            terminalService.printSystemMessage("Request enter the channel.");
        }
        return true;
    }

    private Boolean leave(String[] params) {
        if (userService.isInChannel()) {
            webSocketService.sendMessage(new LeaveRequest());
            terminalService.printSystemMessage("Request leave the channel.");
        }
        return true;
    }

    private Boolean channels(String[] parmas) {
        if (userService.isInLobby()) {
            webSocketService.sendMessage(new FetchChannelsRequest());
            terminalService.printSystemMessage("Request channels list");
        }
        return true;
    }

    private Boolean reject(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new RejectRequest(params[0]));
            terminalService.printSystemMessage("reject invite from %s".formatted(params[0]));
        }
        return true;
    }

    private Boolean disconnect(String[] params) {
        if (userService.isInLobby() && params.length > 0) {
            webSocketService.sendMessage(new DisconnectRequest(params[0]));
            terminalService.printSystemMessage("Disconnect %s".formatted(params[0]));
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
            Commands For Lobby
            '/register' Register a new user. Usage: '/register <Username> <Password>'
            '/unregister' Unregister a user. Usage: '/unregister'
            '/login' Login a user. Usage: '/login <Username> <Password>'
            '/inviteCode' Get inviteCode of yours or joined channel. Usage: '/inviteCode user or /inviteCode channel <ChannelId>'
            '/invite' Invite a user. Usage: '/invite <InviteCode>'
            '/accept' Accept invite from a user. Usage: '/accept <Username>'
            '/pending' Get pending list. Usage: '/pending'
            '/reject' Reject invite from a user. Usage: '/reject <Username>'
            '/disconnect' Disconnect from a user. Usage: '/disconnect <Username>'
            '/connections' Get connections list. Usage: '/connections'
            '/create' Create a channel. (Up to 99 users) Usage: '/create <Title> <Username> ...'
            '/join'  Join the channel. Usage: '/join <InviteCode>'
            '/enter' Enter the channel. Usage: '/enter <ChannelId>'
            '/leave' Leave the channel. Usage: '/leave
            '/quit'  Quit the channel. Usage: '/quit
            '/channels' Get joined channels list. Usage: '/channels'

            Commands For Channel

            Commands For Lobby/Channel
            '/logout' Logout a user. Usage: '/logout'
            '/clear' Clear terminal. Usage: '/clear'
            '/exit' Exit the client. Usage: '/exit'
        """);

        return true;
    }
}
