package com.aws.memento.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class ProgressWebSocketHandler(
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val sessionId = session.id
        sessions[sessionId] = session
        logger.info("WebSocket connected: sessionId={}", sessionId)

        val connectMessage =
            mapOf(
                "type" to "CONNECTED",
                "sessionId" to sessionId,
                "timestamp" to System.currentTimeMillis(),
            )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(connectMessage)))
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        logger.info("Received message from sessionId={}: {}", session.id, message.payload)
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        val sessionId = session.id
        sessions.remove(sessionId)
        logger.info("WebSocket disconnected: sessionId={}, status={}", sessionId, status)
    }

    fun sendProgressUpdate(
        sessionId: String,
        step: String,
        progress: Int,
        message: String,
    ) {
        val session = sessions[sessionId] ?: return

        if (!session.isOpen) {
            logger.warn("Session {} is not open", sessionId)
            sessions.remove(sessionId)
            return
        }

        val progressMessage =
            mapOf(
                "type" to "PROGRESS",
                "step" to step,
                "progress" to progress,
                "message" to message,
                "timestamp" to System.currentTimeMillis(),
            )

        try {
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(progressMessage)))
            logger.info("Sent progress to sessionId={}: {}%", sessionId, progress)
        } catch (e: Exception) {
            logger.error("Failed to send message to session {}", sessionId, e)
            sessions.remove(sessionId)
        }
    }

    fun sendComplete(
        sessionId: String,
        result: Map<String, Any>,
    ) {
        val session = sessions[sessionId] ?: return

        val completeMessage =
            mapOf(
                "type" to "COMPLETE",
                "result" to result,
                "timestamp" to System.currentTimeMillis(),
            )

        try {
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(completeMessage)))
            logger.info("Sent complete message to sessionId={}", sessionId)
        } catch (e: Exception) {
            logger.error("Failed to send complete message to session {}", sessionId, e)
        }
    }

    fun sendError(
        sessionId: String,
        error: String,
    ) {
        val session = sessions[sessionId] ?: return

        val errorMessage =
            mapOf(
                "type" to "ERROR",
                "error" to error,
                "timestamp" to System.currentTimeMillis(),
            )

        try {
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(errorMessage)))
            logger.error("Sent error message to sessionId={}: {}", sessionId, error)
        } catch (e: Exception) {
            logger.error("Failed to send error message to session {}", sessionId, e)
        }
    }
}
