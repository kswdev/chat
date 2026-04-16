package net.study.messageconnectionflux.handler.request;

import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.dto.websocket.inbound.BaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RequestDispatcher {

    private final Map<Class<? extends BaseRequest>, BaseRequestHandler<? extends BaseRequest>> handlers = new HashMap<>();

    @Autowired
    private RequestDispatcher(List<BaseRequestHandler<? extends BaseRequest>> baseRequestHandlers) {
        for (BaseRequestHandler<? extends BaseRequest> handler : baseRequestHandlers) {
            Class<? extends BaseRequest> type = handler.getRequestType();
            this.handlers.put(type, handler);
        }
    }

    public Mono<Void> dispatch(WebSocketSession session, BaseRequest request) {
        return Mono.justOrEmpty(this.getHandler(request))
                .map(this::castToSpecificHandler)
                .flatMap(handler -> Mono.fromRunnable(() -> handler.handleRequest(session, request)))
                .onErrorContinue((err, __) -> loggingIfNoSuchHandler(request))
                .then();
    }

    private BaseRequestHandler<? extends BaseRequest> getHandler(BaseRequest request) {
        return handlers.get(request.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseRequest> BaseRequestHandler<T> castToSpecificHandler(BaseRequestHandler<? extends BaseRequest> handler) {
        return (BaseRequestHandler<T>) handler;
    }

    private void loggingIfNoSuchHandler(BaseRequest request) {
        log.error("No suitable handler found for request type: {}", request.getClass().getName());
    }
}
