package com.aws.memento.websocket

import com.aws.memento.controller.dto.ImageGenerationRequest
import com.aws.memento.service.GeminiImageService
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

@Component
class ImageGenerationWebSocketHandler(
    private val geminiImageService: GeminiImageService,
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sessions = mutableSetOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        logger.info("이미지 생성 WebSocket 연결: ${session.id}")
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        sessions.remove(session)
        logger.info("이미지 생성 WebSocket 종료: ${session.id}")
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        try {
            val request = objectMapper.readValue(message.payload, ImageGenerationRequest::class.java)
            logger.info("이미지 생성 요청: ${request.imageFileNames.size}개 이미지, 스타일: ${request.style}")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    geminiImageService
                        .analyzeImagesAndGenerate(
                            request.imageFileNames,
                            request.style,
                            request.additionalPrompt,
                        ).collect { progress ->
                            if (session.isOpen) {
                                session.sendMessage(TextMessage(progress))
                            }
                        }
                } catch (e: Exception) {
                    logger.error("이미지 생성 중 오류", e)
                    if (session.isOpen) {
                        session.sendMessage(TextMessage("ERROR:${e.message}"))
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("메시지 처리 오류", e)
            if (session.isOpen) {
                session.sendMessage(TextMessage("ERROR:요청 파싱 실패"))
            }
        }
    }
}
