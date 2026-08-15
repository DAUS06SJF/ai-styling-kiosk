package com.hackathon.styling.domain.styling.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.styling.domain.styling.dto.StylingRecommendationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StylingRecommendationWebSocketHandlerTest {

    @Test
    void broadcastsCreatedRecommendationAsJson() throws Exception {
        StylingRecommendationWebSocketHandler handler =
                new StylingRecommendationWebSocketHandler(new ObjectMapper());
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        handler.afterConnectionEstablished(session);

        StylingRecommendationResponse response = new StylingRecommendationResponse(
                42L,
                null,
                "데일리",
                "MINIMAL",
                List.of("검정"),
                "미니멀 룩",
                "톤을 맞추세요.",
                List.of(),
                "http://localhost:8080/generated-stylings/look.png",
                null
        );
        handler.broadcastCreated(response);

        ArgumentCaptor<TextMessage> message = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(message.capture());
        assertThat(message.getValue().getPayload())
                .contains("STYLING_RECOMMENDATION_CREATED")
                .contains("\"id\":42")
                .contains("look.png");
    }
}
