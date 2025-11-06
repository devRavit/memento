package com.aws.memento.websocket

import com.aws.memento.controller.dto.ChatRequest
import com.aws.memento.service.GeminiApiService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(
    private val geminiApiService: GeminiApiService,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
        logger.info("Chat WebSocket 연결: ${session.id}")

        val connectedMessage =
            mapOf(
                "type" to "CONNECTED",
                "sessionId" to session.id,
                "message" to "Chat WebSocket에 연결되었습니다",
            )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(connectedMessage)))
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        sessions.remove(session.id)
        logger.info("Chat WebSocket 연결 종료: ${session.id}")
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        try {
            val chatRequest = objectMapper.readValue(message.payload, ChatRequest::class.java)

            sendStartMessage(session)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    geminiApiService
                        .streamChat(chatRequest.message, chatRequest.conversationHistory)
                        .collect { chunk ->
                            sendChunk(session, chunk)
                        }
                    sendComplete(session)
                } catch (e: Exception) {
                    logger.error("Chat 스트리밍 중 오류 발생", e)
                    sendError(session, e.message ?: "알 수 없는 오류")
                }
            }
        } catch (e: Exception) {
            logger.error("메시지 처리 중 오류 발생", e)
            sendError(session, "메시지 처리 실패: ${e.message}")
        }
    }

    private fun sendStartMessage(session: WebSocketSession) {
        val message =
            mapOf(
                "type" to "CHAT_START",
                "timestamp" to System.currentTimeMillis(),
            )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }

    private fun sendChunk(
        session: WebSocketSession,
        chunk: String,
    ) {
        val message =
            mapOf(
                "type" to "CHAT_CHUNK",
                "chunk" to chunk,
                "timestamp" to System.currentTimeMillis(),
            )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }

    private fun sendComplete(session: WebSocketSession) {
        val message =
            mapOf(
                "type" to "CHAT_COMPLETE",
                "timestamp" to System.currentTimeMillis(),
            )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }

    private fun sendError(
        session: WebSocketSession,
        error: String,
    ) {
        val message =
            mapOf(
                "type" to "CHAT_ERROR",
                "error" to error,
                "timestamp" to System.currentTimeMillis(),
            )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(message)))
    }
}
