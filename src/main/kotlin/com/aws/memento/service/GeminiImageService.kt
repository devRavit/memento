package com.aws.memento.service

import com.aws.memento.domain.ImageStyle
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
import java.io.File
import java.util.Base64
import java.util.concurrent.TimeUnit

data class GeminiVisionRequest(
    val contents: List<GeminiVisionContent>,
)

data class GeminiVisionContent(
    val role: String,
    val parts: List<GeminiVisionPart>,
)

data class GeminiVisionPart(
    val text: String? = null,
    val inlineData: InlineData? = null,
)

data class InlineData(
    val mimeType: String,
    val data: String,
)

data class GeminiImageGenerationRequest(
    val contents: List<GeminiVisionContent>,
)

@Service
class GeminiImageService(
    @Value("\${gemini.api.key:}")
    private val apiKey: String,
    private val objectMapper: ObjectMapper,
    private val fileStorageService: FileStorageService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val client =
        OkHttpClient
            .Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

    private val visionModel = "gemini-2.5-flash"
    private val imageModel = "gemini-2.5-flash-image"

    fun analyzeImagesAndGenerate(
        imageFileNames: List<String>,
        style: ImageStyle,
        additionalPrompt: String?,
    ): Flow<String> =
        flow {
            try {
                emit("이미지 분석 시작...")

                val imageParts =
                    imageFileNames.map { fileName ->
                        val file = fileStorageService.loadFile(fileName)
                        val base64 = encodeFileToBase64(file)
                        val mimeType = when (file.extension.lowercase()) {
                            "jpg" -> "image/jpeg"
                            "png" -> "image/png"
                            "gif" -> "image/gif"
                            "webp" -> "image/webp"
                            else -> "image/${file.extension}"
                        }
                        GeminiVisionPart(
                            inlineData =
                                InlineData(
                                    mimeType = mimeType,
                                    data = base64,
                                ),
                        )
                    }

                val analysisPrompt = buildAnalysisPrompt(style, additionalPrompt)
                val analysisRequest =
                    GeminiVisionRequest(
                        contents =
                            listOf(
                                GeminiVisionContent(
                                    role = "user",
                                    parts =
                                        imageParts + GeminiVisionPart(text = analysisPrompt),
                                ),
                            ),
                    )

                emit("이미지 분석 중...")
                val nanoBananaPrompt = callGeminiVision(analysisRequest)
                emit("분석 완료! 프롬프트 생성됨")
                emit("생성된 프롬프트: $nanoBananaPrompt")

                emit("이미지 생성 시작...")
                val generatedImageData = generateImage(nanoBananaPrompt, imageParts)
                emit("이미지 생성 완료!")

                val savedFileName = saveGeneratedImage(generatedImageData)
                emit("SUCCESS:$savedFileName")
            } catch (e: Exception) {
                logger.error("이미지 생성 실패", e)
                emit("ERROR:${e.message}")
            }
        }

    private fun buildAnalysisPrompt(
        style: ImageStyle,
        additionalPrompt: String?,
    ): String {
        val basePrompt =
            """
            Analyze these images and create an extremely detailed, high-quality prompt for professional image generation.

            Target style: ${style.displayName} - ${style.description}
            ${additionalPrompt?.let { "Additional requirements: $it" } ?: ""}

            Create a HIGHLY DETAILED prompt with:
            1. Scene Description: Full narrative of what's happening, not just keywords
            2. Visual Details: Colors, textures, materials, patterns, specific objects
            3. Lighting: Type of lighting (natural/artificial), direction, quality (soft/harsh), color temperature
            4. Composition: Camera angle (eye-level/bird's-eye/low-angle), framing, depth of field
            5. Atmosphere & Mood: Emotional tone, time of day, weather conditions
            6. Technical Quality: "professional photography", "high resolution", "detailed", "sharp focus"
            7. Style Application: Seamlessly integrate ${style.description} throughout
            8. Character Details (if applicable): Facial expressions, clothing details, poses, interactions

            Use descriptive adjectives and specific nouns. Aim for 150-250 words of vivid description.
            Write as a continuous narrative, not bullet points.

            Return ONLY the image generation prompt, nothing else.
            """.trimIndent()

        return basePrompt
    }

    private fun callGeminiVision(request: GeminiVisionRequest): String {
        val apiUrl = "https://generativelanguage.googleapis.com/v1/models/$visionModel:generateContent?key=$apiKey"

        val json = objectMapper.writeValueAsString(request)
        logger.info("Gemini Vision API 요청")

        val httpRequest =
            Request
                .Builder()
                .url(apiUrl)
                .post(json.toRequestBody("application/json".toMediaType()))
                .addHeader("content-type", "application/json")
                .build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "응답 본문 없음"
                logger.error("Gemini Vision API 오류: ${response.code} - $errorBody")
                throw RuntimeException("Gemini Vision API 오류: ${response.code}")
            }

            val responseBody =
                response.body?.string() ?: throw RuntimeException("응답 본문이 비어있습니다")
            logger.info("Gemini Vision API 응답 수신")

            val jsonNode = objectMapper.readTree(responseBody)
            val candidates = jsonNode.get("candidates")
            if (candidates != null && candidates.isArray && candidates.size() > 0) {
                val content = candidates[0].get("content")
                val parts = content?.get("parts")
                if (parts != null && parts.isArray && parts.size() > 0) {
                    return parts[0].get("text")?.asText()
                        ?: throw RuntimeException("텍스트 응답이 없습니다")
                }
            }
            throw RuntimeException("유효한 응답을 받지 못했습니다")
        }
    }

    private fun generateImage(
        prompt: String,
        referenceImages: List<GeminiVisionPart>,
    ): String {
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$imageModel:generateContent"

        val request =
            GeminiImageGenerationRequest(
                contents =
                    listOf(
                        GeminiVisionContent(
                            role = "user",
                            parts = referenceImages + GeminiVisionPart(text = prompt),
                        ),
                    ),
            )

        val json = objectMapper.writeValueAsString(request)
        logger.info("Gemini Image Generation API 요청")

        val httpRequest =
            Request
                .Builder()
                .url(apiUrl)
                .post(json.toRequestBody("application/json".toMediaType()))
                .addHeader("content-type", "application/json")
                .addHeader("x-goog-api-key", apiKey)
                .build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "응답 본문 없음"
                logger.error("Gemini Image API 오류: ${response.code} - $errorBody")
                throw RuntimeException("Gemini Image API 오류: ${response.code}")
            }

            val responseBody =
                response.body?.string() ?: throw RuntimeException("응답 본문이 비어있습니다")
            logger.info("Gemini Image API 응답 수신")
            logger.info("응답 본문 (처음 500자): ${responseBody.take(500)}")

            val jsonNode = objectMapper.readTree(responseBody)
            val candidates = jsonNode.get("candidates")
            if (candidates != null && candidates.isArray && candidates.size() > 0) {
                val content = candidates[0].get("content")
                val parts = content?.get("parts")
                if (parts != null && parts.isArray && parts.size() > 0) {
                    val inlineData = parts[0].get("inlineData")
                    val imageData = inlineData?.get("data")?.asText()
                    if (imageData != null) {
                        return imageData
                    }
                }
            }
            throw RuntimeException("생성된 이미지 데이터가 없습니다")
        }
    }

    private fun encodeFileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun saveGeneratedImage(base64Data: String): String {
        val imageBytes = Base64.getDecoder().decode(base64Data)
        val fileName = "generated_${System.currentTimeMillis()}.png"
        val savedFile = fileStorageService.saveGeneratedImage(fileName, imageBytes)
        return savedFile.name
    }
}
