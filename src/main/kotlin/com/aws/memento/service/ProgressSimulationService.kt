package com.aws.memento.service

import com.aws.memento.websocket.ProgressWebSocketHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProgressSimulationService(
    private val progressWebSocketHandler: ProgressWebSocketHandler,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun startSimulation(sessionId: String) {
        logger.info("Starting progress simulation for sessionId={}", sessionId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                progressWebSocketHandler.sendProgressUpdate(
                    sessionId = sessionId,
                    step = "UPLOAD",
                    progress = 10,
                    message = "이미지 업로드 중...",
                )
                delay(2000)

                progressWebSocketHandler.sendProgressUpdate(
                    sessionId = sessionId,
                    step = "ANALYZE",
                    progress = 30,
                    message = "AI 이미지 분석 중...",
                )
                delay(3000)

                progressWebSocketHandler.sendProgressUpdate(
                    sessionId = sessionId,
                    step = "GENERATE",
                    progress = 60,
                    message = "기념품 디자인 생성 중...",
                )
                delay(4000)

                progressWebSocketHandler.sendProgressUpdate(
                    sessionId = sessionId,
                    step = "OPTIMIZE",
                    progress = 85,
                    message = "이미지 최적화 중...",
                )
                delay(2000)

                progressWebSocketHandler.sendProgressUpdate(
                    sessionId = sessionId,
                    step = "COMPLETE",
                    progress = 100,
                    message = "처리 완료!",
                )
                delay(500)

                val result =
                    mapOf(
                        "imageUrl" to "https://example.com/result.jpg",
                        "processingTime" to "11.5초",
                        "style" to "수채화",
                    )

                progressWebSocketHandler.sendComplete(sessionId, result)
                logger.info("Progress simulation completed for sessionId={}", sessionId)
            } catch (e: Exception) {
                logger.error("Error during simulation for sessionId={}", sessionId, e)
                progressWebSocketHandler.sendError(sessionId, "처리 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
}
