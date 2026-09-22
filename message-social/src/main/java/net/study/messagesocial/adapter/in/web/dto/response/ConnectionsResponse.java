package net.study.messagesocial.adapter.in.web.dto.response;

import java.util.List;

public record ConnectionsResponse(List<ConnectionSummary> connections) {

    public record ConnectionSummary(Long userId, String status) { }
}
