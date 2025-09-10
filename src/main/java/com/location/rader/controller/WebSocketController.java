package com.location.rader.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.location.rader.service.WebSocketSessionService;

@Controller
public class WebSocketController {

	@Autowired
	private WebSocketSessionService webSocketSessionService;

	private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);


	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent connectedEvent) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(connectedEvent.getMessage());
		String sessionId = stompHeaderAccessor.getSessionId();

		log.info("New WebSocket connection, session ID: {}", sessionId);

		// You could store this sessionId somewhere if needed
	}

	@EventListener
	public void handleWebSocketSubscribeListener(SessionSubscribeEvent subscribeEvent) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(subscribeEvent.getMessage());
		String sessionId = stompHeaderAccessor.getSessionId();
		String destination = stompHeaderAccessor.getDestination();

		if (sessionId != null && destination != null && destination.startsWith("/topic/coordinates/")) {
			Long userId = Long.parseLong(destination.substring("/topic/coordinates/".length()));
			webSocketSessionService.mappingSessionIdToUserId(userId, sessionId);
		}
	}
}
