package com.aws.memento.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

@Service
class GoodsPreviewService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val generatedDirectory = File("upload/generated")

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

        val userImage = loadImageFromUrl(imageUrl)
        val previewImage = createGoodsPreview(userImage, goodsType)

        val fileName = "goods_preview_${System.currentTimeMillis()}.png"
        val outputFile = File(generatedDirectory, fileName)
        ImageIO.write(previewImage, "PNG", outputFile)

        val imageUrl = "http://localhost:9998/api/v1/files/download/$fileName"
        logger.info("굿즈 미리보기 생성 완료: $imageUrl")

        return imageUrl
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
