package net.study.messagesystem.handler.websocket;

import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.websocket.inbound.BaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
public class RequestHandlerDispatcher {

    private final Map<Class<? extends BaseRequest>, BaseRequestHandler<? extends BaseRequest>> handlers = new HashMap<>();

    @Autowired
    private RequestHandlerDispatcher(List<BaseRequestHandler<? extends BaseRequest>> baseRequestHandlers) {
        for (BaseRequestHandler<? extends BaseRequest> handler : baseRequestHandlers) {
            Class<? extends BaseRequest> type = handler.getRequestType();
            this.handlers.put(type, handler);
        }
    }

    public void dispatch(WebSocketSession session, BaseRequest request) {
        Optional.ofNullable(this.getHandler(request))
                .map(this::castToSpecificHandler)
                .ifPresentOrElse(handle(session, request), loggingIfNoSuchHandler(request));
    }

    private BaseRequestHandler<? extends BaseRequest> getHandler(BaseRequest request) {
        return handlers.get(request.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseRequest> BaseRequestHandler<T> castToSpecificHandler(BaseRequestHandler<? extends BaseRequest> handler) {
        return (BaseRequestHandler<T>) handler;
    }

    private static Consumer<BaseRequestHandler<BaseRequest>> handle(WebSocketSession session, BaseRequest request) {
        return handler -> handler.handleRequest(session, request);
    }

    private static Runnable loggingIfNoSuchHandler(BaseRequest request) {
        return () -> log.error("No suitable handler found for request type: {}", request.getClass().getName());
    }
}
