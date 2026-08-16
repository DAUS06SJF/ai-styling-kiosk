package com.hackathon.styling.domain.styling.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class StylingRecommendationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.debug("코디 웹소켓 연결: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.debug("코디 웹소켓 종료: {} ({})", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
        log.warn("코디 웹소켓 전송 오류: {}", session.getId(), exception);
    }

    public void broadcastCreated(StylingRecommendationResponse response) {
        broadcast(StylingRecommendationWebSocketEvent.created(response));
    }

    public void broadcastSelected(StylingRecommendationResponse response) {
        broadcast(StylingRecommendationWebSocketEvent.selected(response));
    }

    private void broadcast(StylingRecommendationWebSocketEvent event) {
        final TextMessage message;
        try {
            message = new TextMessage(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            log.error("코디 웹소켓 메시지 직렬화에 실패했습니다.", exception);
            return;
        }

        sessions.removeIf(session -> !session.isOpen());
        sessions.forEach(session -> send(session, message));
    }

    private void send(WebSocketSession session, TextMessage message) {
        try {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException exception) {
            sessions.remove(session);
            log.warn("코디 웹소켓 메시지 전송에 실패했습니다: {}", session.getId(), exception);
        }
    }
}
