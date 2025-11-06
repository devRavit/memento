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
                emit("PROGRESS:10:이미지를 불러오는 중...")

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

                emit("PROGRESS:30:이미지 품질을 분석하는 중...")

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

                val nanoBananaPrompt = callGeminiVision(analysisRequest)
                emit("PROGRESS:60:AI가 최적의 보정 방법을 찾는 중...")

                emit("PROGRESS:70:보정된 이미지를 생성하는 중...")
                val generatedImageData = generateImage(nanoBananaPrompt, imageParts)
                emit("PROGRESS:90:이미지를 저장하는 중...")

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
            when (style.category) {
                com.aws.memento.domain.StyleCategory.STYLE_TRANSFORM ->
                    """
                    You are creating a prompt for image-to-image style transfer. Analyze the input images and create a prompt that preserves the EXACT composition, layout, and subject matter while applying a new artistic style.

                    Target style: ${style.displayName} - ${style.description}
                    ${additionalPrompt?.let { "Additional requirements: $it" } ?: ""}

                    CRITICAL RULES:
                    1. PRESERVE EXACT COMPOSITION: Keep the same camera angle, framing, and spatial layout as the original
                    2. PRESERVE SUBJECTS: Keep all people, objects, and elements in the SAME positions and poses
                    3. PRESERVE STRUCTURE: Maintain the same background, foreground, and overall scene structure
                    4. ONLY CHANGE STYLE: Apply ${style.description} ONLY to the rendering style, colors, and artistic treatment

                    Your prompt should describe:
                    - The exact scene composition and layout from the original image
                    - Positions and poses of all subjects (people, objects)
                    - Spatial relationships between elements
                    - Then specify: "rendered in ${style.description} style"
                    - Include: "maintaining exact composition and layout from reference image"

                    Keep it concise (100-150 words) and focus on PRESERVING the original while ONLY changing the artistic style.
                    Write as a single descriptive paragraph.

                    Return ONLY the image generation prompt, nothing else.
                    """.trimIndent()

                com.aws.memento.domain.StyleCategory.PHOTO_ENHANCEMENT ->
                    """
                    CRITICAL INSTRUCTION: You are analyzing a photo for ENHANCEMENT ONLY, NOT REGENERATION.

                    Your task: Create an EXTREMELY DETAILED description of the EXACT photo for faithful reproduction with quality improvements.

                    Enhancement goal: ${style.displayName} - ${
                        when (style) {
                            com.aws.memento.domain.ImageStyle.BRIGHT -> "Increase brightness and exposure naturally while preserving all details"
                            com.aws.memento.domain.ImageStyle.VIVID -> "Enhance color saturation and vibrancy naturally without oversaturation"
                            com.aws.memento.domain.ImageStyle.CLARITY -> "Sharpen details and reduce blur while maintaining natural appearance"
                            com.aws.memento.domain.ImageStyle.PROFESSIONAL -> "Comprehensive enhancement: optimize brightness, enhance colors, sharpen details, reduce noise"
                            else -> style.description
                        }
                    }
                    ${additionalPrompt?.let { "\nAdditional: $it" } ?: ""}

                    MANDATORY RULES - VIOLATION WILL FAIL:
                    1. DO NOT REGENERATE - ONLY ENHANCE THE EXACT PHOTO
                    2. PRESERVE EVERY PIXEL'S POSITION - Camera angle, framing, crop, and composition MUST BE IDENTICAL
                    3. PRESERVE ALL SUBJECTS - Every person, object, element MUST stay in EXACT same position, pose, size, and location
                    4. PRESERVE BACKGROUND - Every background element, texture, and detail MUST remain unchanged
                    5. NO CREATIVE CHANGES - Do NOT add, remove, or modify any elements
                    6. MAINTAIN PHOTOGRAPHIC REALISM - Result MUST look like a real photograph, not edited or artificial

                    Required output format:
                    "This is an exact copy of the original photograph showing [EXTREMELY DETAILED PIXEL-LEVEL DESCRIPTION]:
                    - Exact camera angle, distance, and perspective: [describe]
                    - Every subject with precise position, pose, expression, clothing: [describe in detail]
                    - Complete background description with all elements: [describe every detail]
                    - Lighting direction and quality: [describe]
                    - Color palette of the scene: [describe]

                    ENHANCEMENT INSTRUCTION: Apply ${style.displayName} enhancement (${
                        when (style) {
                            com.aws.memento.domain.ImageStyle.BRIGHT -> "increase brightness by adjusting exposure curve while preserving highlight and shadow detail"
                            com.aws.memento.domain.ImageStyle.VIVID -> "boost color saturation by 20-30% while maintaining natural skin tones"
                            com.aws.memento.domain.ImageStyle.CLARITY -> "apply unsharp mask to enhance edge definition without introducing artifacts"
                            com.aws.memento.domain.ImageStyle.PROFESSIONAL -> "apply professional-grade color grading, exposure optimization, and detail enhancement"
                            com.aws.memento.domain.ImageStyle.BLEMISH_REMOVAL -> "remove skin blemishes, acne, spots, and minor imperfections using frequency separation technique while maintaining natural skin texture"
                            com.aws.memento.domain.ImageStyle.SKIN_SMOOTHING -> "apply subtle skin smoothing with selective gaussian blur on low-frequency layer, preserving pores and natural texture details"
                            com.aws.memento.domain.ImageStyle.FACE_ENHANCE -> "enhance facial features with natural beauty adjustments: subtle eye brightening, gentle contouring, and balanced facial proportions"
                            com.aws.memento.domain.ImageStyle.WHITENING -> "brighten skin tone by 15-25% using curves adjustment, maintaining realistic undertones and avoiding oversaturation"
                            com.aws.memento.domain.ImageStyle.BACKGROUND_PEOPLE_REMOVAL -> "intelligently detect and remove all people in the background using content-aware fill and inpainting techniques, while keeping the main subjects (people in foreground) completely intact and untouched. Seamlessly fill removed areas with appropriate background textures matching surrounding environment"
                            else -> "enhance photo quality"
                        }
                    }) to THIS EXACT PHOTOGRAPH without altering composition, subjects, or scene structure. The result must be indistinguishable from the original except for improved quality."

                    IMPORTANT: Describe in 150-200 words with extreme detail. Focus on EXACT replication with quality improvement ONLY.

                    Return ONLY the prompt text.
                    """.trimIndent()
            }

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
