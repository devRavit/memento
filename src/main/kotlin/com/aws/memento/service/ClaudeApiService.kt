package com.aws.memento.service

import com.aws.memento.controller.dto.ChatMessage
import com.fasterxml.jackson.annotation.JsonProperty
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

data class ClaudeMessageRequest(
    val model: String,
    @JsonProperty("max_tokens")
    val maxTokens: Int,
    val messages: List<ClaudeMessage>,
    val stream: Boolean = true,
)

data class ClaudeMessage(
    val role: String,
    val content: String,
)

@Service
class ClaudeApiService(
    @Value("\${anthropic.api.key:}")
    private val apiKey: String,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client =
        OkHttpClient
            .Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

    private val apiUrl = "https://api.anthropic.com/v1/messages"
    private val anthropicVersion = "2023-06-01"

    fun streamChat(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
    ): Flow<String> =
        flow {
            try {
                val messages =
                    conversationHistory.map { msg ->
                        ClaudeMessage(role = msg.role, content = msg.content)
                    } + ClaudeMessage(role = "user", content = userMessage)

                val requestBody =
                    ClaudeMessageRequest(
                        model = "claude-3-5-sonnet-20241022",
                        maxTokens = 4096,
                        messages = messages,
                        stream = true,
                    )

                val json = objectMapper.writeValueAsString(requestBody)
                logger.info("Claude API 요청: $json")

                val request =
                    Request
                        .Builder()
                        .url(apiUrl)
                        .post(json.toRequestBody("application/json".toMediaType()))
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", anthropicVersion)
                        .addHeader("content-type", "application/json")
                        .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "응답 본문 없음"
                        logger.error("Claude API 오류: ${response.code} - ${response.message}, Body: $errorBody")
                        emit("[오류] Claude API 오류: ${response.code} - $errorBody")
                        return@flow
                    }

                    val responseBody =
                        response.body ?: run {
                            emit("[오류] 응답 본문이 비어있습니다")
                            return@flow
                        }

                    responseBody.source().use { source ->
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: continue

                            if (line.startsWith("data: ")) {
                                val data = line.substring(6)

                                if (data == "[DONE]") {
                                    break
                                }

                                try {
                                    val jsonNode = objectMapper.readTree(data)
                                    val type = jsonNode.get("type")?.asText()

                                    if (type == "content_block_delta") {
                                        val delta = jsonNode.get("delta")
                                        val deltaType = delta?.get("type")?.asText()

                                        if (deltaType == "text_delta") {
                                            val text = delta?.get("text")?.asText()
                                            if (text != null) {
                                                emit(text)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    logger.debug("JSON 파싱 스킵: $data", e)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Claude API 호출 실패", e)
                emit("[오류] Claude API 호출에 실패했습니다: ${e.message}")
            }
        }
}
