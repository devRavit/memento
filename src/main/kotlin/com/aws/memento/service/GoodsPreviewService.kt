package com.aws.memento.service

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

@Service
class GoodsPreviewService(
    @Value("\${gemini.api.key:}")
    private val geminiApiKey: String,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val generatedDirectory = File("upload/generated")
    private val client =
        OkHttpClient
            .Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

    init {
        if (!generatedDirectory.exists()) {
            generatedDirectory.mkdirs()
            logger.info("굿즈 미리보기 저장 디렉토리 생성: ${generatedDirectory.absolutePath}")
        }
    }

    fun generateGoodsPreview(
        imageUrl: String,
        goodsType: String,
    ): String {
        logger.info("굿즈 미리보기 생성 시작: $goodsType, $imageUrl")

        return if (geminiApiKey.isNotEmpty()) {
            generateAiGoodsPreview(imageUrl, goodsType)
        } else {
            generateBasicGoodsPreview(imageUrl, goodsType)
        }
    }

    private fun generateAiGoodsPreview(
        imageUrl: String,
        goodsType: String,
    ): String {
        try {
            val userImage = loadImageFromUrl(imageUrl)
            val base64Image = encodeImageToBase64(userImage)

            val prompt = buildGoodsPreviewPrompt(goodsType)

            val generatedImageData = callGeminiImageGeneration(prompt, base64Image)

            val fileName = "goods_preview_${System.currentTimeMillis()}.png"
            val outputFile = File(generatedDirectory, fileName)
            val imageBytes = Base64.getDecoder().decode(generatedImageData)
            outputFile.writeBytes(imageBytes)

            val resultUrl = "http://localhost:9998/api/v1/files/download/$fileName"
            logger.info("AI 굿즈 미리보기 생성 완료: $resultUrl")

            return resultUrl
        } catch (e: Exception) {
            logger.error("AI 굿즈 미리보기 생성 실패, 기본 방식으로 전환", e)
            return generateBasicGoodsPreview(imageUrl, goodsType)
        }
    }

    private fun generateBasicGoodsPreview(
        imageUrl: String,
        goodsType: String,
    ): String {
        val userImage = loadImageFromUrl(imageUrl)
        val previewImage = createGoodsPreview(userImage, goodsType)

        val fileName = "goods_preview_${System.currentTimeMillis()}.png"
        val outputFile = File(generatedDirectory, fileName)
        ImageIO.write(previewImage, "PNG", outputFile)

        val resultUrl = "http://localhost:9998/api/v1/files/download/$fileName"
        logger.info("기본 굿즈 미리보기 생성 완료: $resultUrl")

        return resultUrl
    }

    private fun buildGoodsPreviewPrompt(goodsType: String): String {
        return when (goodsType) {
            "photobook" ->
                """
                Create a realistic product mockup of a premium hardcover photobook lying on a clean surface.
                The photobook should be slightly open, showing the provided photo on one of the visible pages.
                The cover should be dark brown leather with embossed "PHOTOBOOK" text.
                Use professional product photography lighting with soft shadows.
                The background should be a neutral light surface.
                Make it look like a high-quality commercial product photograph that you'd see in an online store.
                The photo should be clearly visible and well-integrated into the page layout.
                """.trimIndent()

            "calendar" ->
                """
                Create a realistic product mockup of a modern desk calendar standing upright.
                The calendar should show the provided photo in the upper half and a monthly calendar grid below.
                The text "DECEMBER 2025" should be visible.
                Use clean, minimalist design with white background for the calendar part.
                Include professional product photography lighting and a neutral surface underneath.
                Make it look like a premium product photograph for an e-commerce site.
                The photo should be the focal point and clearly visible.
                """.trimIndent()

            "magnet" ->
                """
                Create a realistic product mockup of a premium photo magnet.
                Show the provided photo mounted on a white-bordered square magnet.
                The magnet should be shown on a metallic refrigerator surface with subtle reflections.
                Use professional product photography lighting.
                Include small text "PHOTO MAGNET" at the bottom of the white border.
                Make it look like a high-quality e-commerce product photo.
                """.trimIndent()

            "frame" ->
                """
                Create a realistic product mockup of an elegant photo frame hanging on a light gray wall.
                The frame should be dark wood with a classic design.
                The provided photo should be clearly visible inside the frame with a white matting border.
                Use natural lighting with subtle wall shadows.
                Make it look like a premium home decor product photograph.
                Professional interior photography style.
                """.trimIndent()

            "sticker" ->
                """
                Create a realistic product mockup of glossy photo stickers.
                Show multiple copies of the provided photo as die-cut stickers with a white border.
                Display them on a clean white surface with one slightly peeled up showing the backing.
                Use bright, clean product photography lighting.
                Make it look like a professional e-commerce product photo.
                High-quality print finish with slight gloss reflection.
                """.trimIndent()

            "poster" ->
                """
                Create a realistic product mockup of an A3 poster.
                Show the provided photo as a large print on premium matte paper.
                The poster should be shown flat or slightly rolled at one edge.
                Use clean product photography with neutral background.
                Make it look like a high-quality art print product photo.
                Professional commercial photography style.
                """.trimIndent()

            "postcard" ->
                """
                Create a realistic product mockup of a premium photo postcard.
                Show the provided photo as the main image on a postcard with white borders.
                Display it slightly tilted on a clean surface.
                Use soft product photography lighting.
                Make it look like a boutique stationery product photo.
                High-quality print on thick cardstock.
                """.trimIndent()

            "wall-calendar" ->
                """
                Create a realistic product mockup of a wall calendar.
                Show the provided photo in the upper portion with a monthly calendar grid below.
                The calendar should appear to be hanging on a light wall.
                Include binding holes at the top.
                Use natural interior lighting.
                Make it look like a premium home decor product photograph.
                """.trimIndent()

            else ->
                """
                Create a realistic product mockup showing the provided photo as a premium printed product.
                Use professional product photography with clean background.
                Make it look like a high-quality e-commerce product photo.
                """.trimIndent()
        }
    }

    private fun callGeminiImageGeneration(
        prompt: String,
        base64Image: String,
    ): String {
        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent"

        val requestBody =
            mapOf(
                "contents" to
                    listOf(
                        mapOf(
                            "role" to "user",
                            "parts" to
                                listOf(
                                    mapOf(
                                        "inlineData" to
                                            mapOf(
                                                "mimeType" to "image/png",
                                                "data" to base64Image,
                                            ),
                                    ),
                                    mapOf("text" to prompt),
                                ),
                        ),
                    ),
            )

        val json = objectMapper.writeValueAsString(requestBody)
        logger.info("Gemini Image Generation API 요청: $prompt")

        val httpRequest =
            Request
                .Builder()
                .url(apiUrl)
                .post(json.toRequestBody("application/json".toMediaType()))
                .addHeader("content-type", "application/json")
                .addHeader("x-goog-api-key", geminiApiKey)
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

    private fun encodeImageToBase64(image: BufferedImage): String {
        val outputStream = java.io.ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    private fun loadImageFromUrl(imageUrl: String): BufferedImage {
        return try {
            if (imageUrl.startsWith("http")) {
                ImageIO.read(URL(imageUrl))
            } else {
                val localPath = imageUrl.removePrefix("http://localhost:9998/api/v1/files/download/")
                ImageIO.read(File("upload", localPath))
            }
        } catch (e: Exception) {
            logger.warn("이미지 로드 실패, 기본 이미지 생성: ${e.message}")
            createPlaceholderImage()
        }
    }

    private fun createPlaceholderImage(): BufferedImage {
        val image = BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.color = Color(230, 230, 230)
        g.fillRect(0, 0, 400, 400)
        g.dispose()
        return image
    }

    private fun createGoodsPreview(
        userImage: BufferedImage,
        goodsType: String,
    ): BufferedImage {
        return when (goodsType) {
            "photobook" -> createPhotobookPreview(userImage)
            "calendar" -> createCalendarPreview(userImage)
            "magnet" -> createMagnetPreview(userImage)
            "frame" -> createFramePreview(userImage)
            else -> createDefaultPreview(userImage, goodsType)
        }
    }

    private fun createPhotobookPreview(userImage: BufferedImage): BufferedImage {
        val width = 600
        val height = 800
        val preview = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = preview.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.color = Color(245, 240, 235)
        g.fillRect(0, 0, width, height)

        g.color = Color(139, 69, 19)
        g.fillRect(50, 100, 500, 600)

        val imageWidth = 450
        val imageHeight = 450
        val imageX = 75
        val imageY = 125
        g.drawImage(userImage, imageX, imageY, imageWidth, imageHeight, null)

        g.color = Color(80, 50, 10)
        g.font = Font("Sans-Serif", Font.BOLD, 24)
        g.drawString("PHOTOBOOK", 200, 730)

        g.dispose()
        return preview
    }

    private fun createCalendarPreview(userImage: BufferedImage): BufferedImage {
        val width = 600
        val height = 800
        val preview = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = preview.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.color = Color(250, 250, 250)
        g.fillRect(0, 0, width, height)

        val imageWidth = 500
        val imageHeight = 400
        val imageX = 50
        val imageY = 100
        g.drawImage(userImage, imageX, imageY, imageWidth, imageHeight, null)

        g.color = Color(255, 255, 255)
        g.fillRect(50, 520, 500, 250)

        g.color = Color(200, 200, 200)
        for (i in 0 until 7) {
            for (j in 0 until 5) {
                g.drawRect(50 + i * 71, 520 + j * 50, 71, 50)
            }
        }

        g.color = Color(100, 100, 100)
        g.font = Font("Sans-Serif", Font.BOLD, 32)
        g.drawString("DECEMBER 2025", 150, 60)

        g.dispose()
        return preview
    }

    private fun createMagnetPreview(userImage: BufferedImage): BufferedImage {
        val width = 600
        val height = 600
        val preview = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = preview.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.color = Color(240, 240, 240)
        g.fillRect(0, 0, width, height)

        g.color = Color.WHITE
        g.fillRect(150, 150, 300, 300)

        g.color = Color(220, 220, 220)
        g.fillRect(155, 155, 290, 290)

        val imageWidth = 280
        val imageHeight = 280
        val imageX = 160
        val imageY = 160
        g.drawImage(userImage, imageX, imageY, imageWidth, imageHeight, null)

        g.color = Color(180, 180, 180)
        g.font = Font("Sans-Serif", Font.PLAIN, 16)
        g.drawString("PHOTO MAGNET", 230, 490)

        g.dispose()
        return preview
    }

    private fun createFramePreview(userImage: BufferedImage): BufferedImage {
        val width = 700
        val height = 900
        val preview = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = preview.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.color = Color(235, 235, 235)
        g.fillRect(0, 0, width, height)

        g.color = Color(50, 30, 10)
        g.fillRect(100, 150, 500, 600)

        g.color = Color(255, 255, 255)
        g.fillRect(120, 170, 460, 560)

        val imageWidth = 440
        val imageHeight = 540
        val imageX = 130
        val imageY = 180
        g.drawImage(userImage, imageX, imageY, imageWidth, imageHeight, null)

        g.dispose()
        return preview
    }

    private fun createDefaultPreview(
        userImage: BufferedImage,
        goodsType: String,
    ): BufferedImage {
        val width = 600
        val height = 600
        val preview = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = preview.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.color = Color(240, 240, 240)
        g.fillRect(0, 0, width, height)

        val imageWidth = 400
        val imageHeight = 400
        val imageX = 100
        val imageY = 100
        g.drawImage(userImage, imageX, imageY, imageWidth, imageHeight, null)

        g.color = Color(100, 100, 100)
        g.font = Font("Sans-Serif", Font.BOLD, 24)
        g.drawString(goodsType.uppercase(), 200, 550)

        g.dispose()
        return preview
    }
}
