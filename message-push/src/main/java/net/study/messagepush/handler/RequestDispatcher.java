package net.study.messagepush.handler;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.RecordInterface;
import net.study.messagepush.handler.kafka.BaseRecordHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
public class RequestDispatcher {

    private final Map<Class<? extends RecordInterface>, BaseRecordHandler<? extends RecordInterface>> handlers = new HashMap<>();

    @Autowired
    private RequestDispatcher(List<BaseRecordHandler<? extends RecordInterface>> baseRequestHandlers) {
        for (BaseRecordHandler<? extends RecordInterface> handler : baseRequestHandlers) {
            Class<? extends RecordInterface> type = handler.getRequestType();
            this.handlers.put(type, handler);
        }
    }

    public void dispatch(RecordInterface request) {
        Optional.ofNullable(this.getHandler(request))
                .map(this::castToSpecificHandler)
                .ifPresentOrElse(handle(request), loggingIfNoSuchHandler(request));
    }

    private BaseRecordHandler<? extends RecordInterface> getHandler(RecordInterface request) {
        return handlers.get(request.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends RecordInterface> BaseRecordHandler<T> castToSpecificHandler(BaseRecordHandler<? extends RecordInterface> handler) {
        return (BaseRecordHandler<T>) handler;
    }

    private static Consumer<BaseRecordHandler<RecordInterface>> handle(RecordInterface request) {
        return handler -> handler.handleRequest(request);
    }

    private static Runnable loggingIfNoSuchHandler(RecordInterface request) {
        return () -> log.error("No suitable handler found for request type: {}", request.getClass().getName());
    }
}
