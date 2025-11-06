package com.aws.memento.service

import com.aws.memento.controller.dto.ChatMessage
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

data class GeminiRequest(
    val contents: List<GeminiContent>,
)

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>,
)

data class GeminiPart(
    val text: String,
)

@Service
class GeminiApiService(
    @Value("\${gemini.api.key:}")
    private val apiKey: String,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client =
        OkHttpClient
            .Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

    private val model = "gemini-2.5-flash"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1/models/$model:streamGenerateContent?key=$apiKey"

    fun streamChat(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
    ): Flow<String> =
        flow {
            try {
                val contents = mutableListOf<GeminiContent>()

                conversationHistory.forEach { msg ->
                    val role = if (msg.role == "assistant") "model" else "user"
                    contents.add(
                        GeminiContent(
                            role = role,
                            parts = listOf(GeminiPart(text = msg.content)),
                        ),
                    )
                }

                contents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userMessage)),
                    ),
                )

                val requestBody = GeminiRequest(contents = contents)
                val json = objectMapper.writeValueAsString(requestBody)
                logger.info("Gemini API 요청: $json")

                val request =
                    Request
                        .Builder()
                        .url(apiUrl)
                        .post(json.toRequestBody("application/json".toMediaType()))
                        .addHeader("content-type", "application/json")
                        .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "응답 본문 없음"
                        logger.error("Gemini API 오류: ${response.code} - ${response.message}, Body: $errorBody")
                        emit("[오류] Gemini API 오류: ${response.code} - $errorBody")
                        return@flow
                    }

                    val responseBody =
                        response.body ?: run {
                            emit("[오류] 응답 본문이 비어있습니다")
                            return@flow
                        }

                    responseBody.source().use { source ->
                        val fullResponse = source.readUtf8()
                        logger.info("Gemini API 응답: $fullResponse")

                        try {
                            val jsonNode = objectMapper.readTree(fullResponse)

                            if (jsonNode.isArray) {
                                jsonNode.forEach { item ->
                                    val candidates = item.get("candidates")
                                    if (candidates != null && candidates.isArray && candidates.size() > 0) {
                                        val content = candidates[0].get("content")
                                        val parts = content?.get("parts")

                                        if (parts != null && parts.isArray && parts.size() > 0) {
                                            val text = parts[0].get("text")?.asText()
                                            if (text != null) {
                                                emit(text)
                                            }
                                        }
                                    }
                                }
                            } else {
                                val candidates = jsonNode.get("candidates")
                                if (candidates != null && candidates.isArray && candidates.size() > 0) {
                                    val content = candidates[0].get("content")
                                    val parts = content?.get("parts")

                                    if (parts != null && parts.isArray && parts.size() > 0) {
                                        val text = parts[0].get("text")?.asText()
                                        if (text != null) {
                                            emit(text)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            logger.error("JSON 파싱 실패", e)
                            emit("[오류] 응답 파싱 실패: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Gemini API 호출 실패", e)
                emit("[오류] Gemini API 호출에 실패했습니다: ${e.message}")
            }
        }
}
