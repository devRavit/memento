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

        return if (hasTemplateForGoodsType(goodsType)) {
            generateTemplateBasedPreview(imageUrl, goodsType)
        } else if (geminiApiKey.isNotEmpty()) {
            generateAiGoodsPreview(imageUrl, goodsType)
        } else {
            generateBasicGoodsPreview(imageUrl, goodsType)
        }
    }

    private fun hasTemplateForGoodsType(goodsType: String): Boolean {
        val templatePath = "src/main/resources/templates/goods/${goodsType}_template.png"
        return File(templatePath).exists()
    }

    private fun generateTemplateBasedPreview(
        imageUrl: String,
        goodsType: String,
    ): String {
        try {
            val userImage = loadImageFromUrl(imageUrl)
            val template = loadTemplate(goodsType)

            val compositeImage = overlayImageOnTemplate(userImage, template, goodsType)

            val fileName = "goods_preview_${System.currentTimeMillis()}.png"
            val outputFile = File(generatedDirectory, fileName)
            ImageIO.write(compositeImage, "PNG", outputFile)

            val resultUrl = "http://localhost:9998/api/v1/files/download/$fileName"
            logger.info("템플릿 기반 굿즈 미리보기 생성 완료: $resultUrl")

            return resultUrl
        } catch (e: Exception) {
            logger.error("템플릿 기반 미리보기 생성 실패, AI 방식으로 전환", e)
            return if (geminiApiKey.isNotEmpty()) {
                generateAiGoodsPreview(imageUrl, goodsType)
            } else {
                generateBasicGoodsPreview(imageUrl, goodsType)
            }
        }
    }

    private fun loadTemplate(goodsType: String): BufferedImage {
        val templatePath = "src/main/resources/templates/goods/${goodsType}_template.png"
        val templateFile = File(templatePath)

        if (!templateFile.exists()) {
            throw IllegalArgumentException("템플릿 파일이 없습니다: $templatePath")
        }

        return ImageIO.read(templateFile)
    }

    private fun overlayImageOnTemplate(
        userImage: BufferedImage,
        template: BufferedImage,
        goodsType: String,
    ): BufferedImage {
        val result = BufferedImage(template.width, template.height, BufferedImage.TYPE_INT_ARGB)
        val g = result.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val photoAreas = getPhotoAreasForGoodsType(goodsType, template.width, template.height)

        for (photoArea in photoAreas) {
            val scaledImage = scaleImageToFit(userImage, photoArea.width, photoArea.height)
            g.drawImage(scaledImage, photoArea.x, photoArea.y, photoArea.width, photoArea.height, null)
        }

        g.drawImage(template, 0, 0, null)
        g.dispose()

        return result
    }

    private data class Rectangle(val x: Int, val y: Int, val width: Int, val height: Int)

    private fun getPhotoAreasForGoodsType(
        goodsType: String,
        templateWidth: Int,
        templateHeight: Int,
    ): List<Rectangle> {
        return when (goodsType) {
            "photobook" -> listOf(
                Rectangle(
                    x = (templateWidth * 0.15).toInt(),
                    y = (templateHeight * 0.2).toInt(),
                    width = (templateWidth * 0.4).toInt(),
                    height = (templateHeight * 0.5).toInt(),
                )
            )
            "calendar" -> listOf(
                Rectangle(
                    x = (templateWidth * 0.1).toInt(),
                    y = (templateHeight * 0.15).toInt(),
                    width = (templateWidth * 0.8).toInt(),
                    height = (templateHeight * 0.45).toInt(),
                )
            )
            "magnet" -> listOf(
                Rectangle(x = 130, y = 80, width = 240, height = 240),
                Rectangle(x = 510, y = 90, width = 220, height = 220),
                Rectangle(x = 830, y = 80, width = 240, height = 240),
                Rectangle(x = 130, y = 430, width = 240, height = 240),
                Rectangle(x = 550, y = 480, width = 140, height = 140),
                Rectangle(x = 860, y = 440, width = 220, height = 220),
            )
            "frame" -> listOf(
                Rectangle(
                    x = (templateWidth * 0.2).toInt(),
                    y = (templateHeight * 0.2).toInt(),
                    width = (templateWidth * 0.6).toInt(),
                    height = (templateHeight * 0.6).toInt(),
                )
            )
            "sticker" -> listOf(
                Rectangle(
                    x = (templateWidth * 0.25).toInt(),
                    y = (templateHeight * 0.25).toInt(),
                    width = (templateWidth * 0.3).toInt(),
                    height = (templateHeight * 0.3).toInt(),
                )
            )
            "poster" -> listOf(
                Rectangle(
                    x = (templateWidth * 0.15).toInt(),
                    y = (templateHeight * 0.15).toInt(),
                    width = (templateWidth * 0.7).toInt(),
                    height = (templateHeight * 0.7).toInt(),
                )
            )
            "postcard" -> listOf(
                Rectangle(
                    x = (templateWidth * 0.1).toInt(),
                    y = (templateHeight * 0.1).toInt(),
                    width = (templateWidth * 0.8).toInt(),
                    height = (templateHeight * 0.8).toInt(),
                )
            )
            "wall-calendar" -> listOf(
                Rectangle(
                    x = (templateWidth * 0.1).toInt(),
                    y = (templateHeight * 0.1).toInt(),
                    width = (templateWidth * 0.8).toInt(),
                    height = (templateHeight * 0.4).toInt(),
                )
            )
            else -> listOf(
                Rectangle(
                    x = (templateWidth * 0.2).toInt(),
                    y = (templateHeight * 0.2).toInt(),
                    width = (templateWidth * 0.6).toInt(),
                    height = (templateHeight * 0.6).toInt(),
                )
            )
        }
    }

    private fun scaleImageToFit(
        image: BufferedImage,
        targetWidth: Int,
        targetHeight: Int,
    ): BufferedImage {
        val aspectRatio = image.width.toDouble() / image.height.toDouble()
        val targetAspectRatio = targetWidth.toDouble() / targetHeight.toDouble()

        val scaledWidth: Int
        val scaledHeight: Int

        if (aspectRatio > targetAspectRatio) {
            scaledWidth = targetWidth
            scaledHeight = (targetWidth / aspectRatio).toInt()
        } else {
            scaledHeight = targetHeight
            scaledWidth = (targetHeight * aspectRatio).toInt()
        }

        val scaled = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB)
        val g = scaled.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.drawImage(image, 0, 0, scaledWidth, scaledHeight, null)
        g.dispose()

        return scaled
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
                Create an ultra-realistic, high-end product photograph of a luxury hardcover photobook.

                PRODUCT DETAILS:
                - Premium hardcover photobook with dark brown genuine leather texture
                - Gold embossed "PHOTOBOOK" text on the cover
                - The book should be elegantly positioned at a 45-degree angle, partially open
                - Show 2-3 pages visible, with the provided photo prominently displayed on the right page
                - Thick, high-quality matte paper pages with clean edges
                - Professional bookbinding visible at the spine

                PHOTO PRESENTATION:
                - The provided photo should be printed in ULTRA HIGH QUALITY on the page
                - Full-page or near-full-page layout with minimal white margins
                - The photo should look vibrant, sharp, and professional
                - Ensure the photo's colors and details are perfectly preserved

                LIGHTING & QUALITY:
                - Studio-quality photography with soft, diffused lighting from the top-left
                - Subtle shadows underneath and to the right of the book
                - Highlight the leather texture with gentle light reflections
                - Professional depth of field with slight background blur

                SETTING:
                - Place on a pristine light oak wood surface or clean white marble
                - Ultra-clean background with no distractions
                - Premium e-commerce photography standard (Apple/luxury brand quality)
                - The overall image should evoke luxury and premium quality

                STYLE: Professional commercial product photography, luxury stationery aesthetic, museum-quality presentation
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
                Create a hyper-realistic product photograph of premium photo stickers in a professional studio setting.

                COMPOSITION:
                - Show 3-4 identical stickers of the provided photo arranged artistically on a pristine white surface
                - One sticker should be slightly lifted/peeled at the corner to show depth and the paper backing
                - The stickers should have a clean white border (2-3mm) around the photo
                - Arrange them in a casual yet organized layout with slight overlapping

                QUALITY & DETAILS:
                - Ultra-high resolution product photography quality (like you'd see on Apple or Amazon product pages)
                - The photo on each sticker should be crystal clear and vibrant
                - Show subtle glossy reflections on the sticker surface indicating premium vinyl material
                - Capture the texture and thickness of high-quality sticker paper
                - The white border should have clean, precise die-cut edges

                LIGHTING & ATMOSPHERE:
                - Professional studio lighting with soft shadows
                - Bright, clean lighting that highlights the glossy finish
                - Subtle highlights on the glossy surface showing premium quality
                - Pure white background with no distractions

                STYLE:
                - Premium e-commerce product photography aesthetic
                - Focus on making the product look desirable and high-quality
                - Commercial photography standard similar to luxury stationery brands
                - The photo quality should be so good that customers can clearly see every detail

                IMPORTANT: The original photo should be PERFECTLY preserved and clearly visible on each sticker. This is a product photo to sell stickers, so the photo quality and clarity is paramount.
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
