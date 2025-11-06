package com.aws.memento.service

import com.aws.memento.controller.dto.CreateSouvenirRequest
import com.aws.memento.domain.Souvenir
import org.springframework.stereotype.Service

interface SouvenirService {
    fun createSouvenir(request: CreateSouvenirRequest): Souvenir

    fun getSouvenir(id: String): Souvenir?

    fun listSouvenirs(userId: String): List<Souvenir>
}

@Service
class SouvenirServiceImpl(
    private val s3Service: S3Service,
    private val geminiService: GeminiService,
    private val novaCanvasService: NovaCanvasService,
) : SouvenirService {
    override fun createSouvenir(request: CreateSouvenirRequest): Souvenir {
        val souvenirId = generateSouvenirId()

        val uploadedImageUrls =
            request.images.map { image ->
                s3Service.uploadImage(souvenirId, image.fileName, image.base64Data)
            }

        val souvenir =
            Souvenir(
                id = souvenirId,
                userId = request.userId,
                status = com.aws.memento.domain.SouvenirStatus.ANALYZING,
                inputImageUrls = uploadedImageUrls,
                style = request.preferredStyle,
                productType = request.preferredProductType,
                analysis = null,
                generatedImageUrl = null,
                createdAt = java.time.Instant.now(),
                updatedAt = java.time.Instant.now(),
            )

        val analysis =
            geminiService.analyzeImages(
                imageUrls = uploadedImageUrls,
                preferredStyle = request.preferredStyle,
                preferredProductType = request.preferredProductType,
            )

        val generatedImageUrl = novaCanvasService.generateImage(analysis.novaCanvasPrompt)

        return souvenir.copy(
            status = com.aws.memento.domain.SouvenirStatus.COMPLETED,
            analysis = analysis,
            generatedImageUrl = generatedImageUrl,
            updatedAt = java.time.Instant.now(),
        )
    }

    override fun getSouvenir(id: String): Souvenir? {
        return null
    }

    override fun listSouvenirs(userId: String): List<Souvenir> {
        return emptyList()
    }

    private fun generateSouvenirId(): String {
        return "sov_${java.util.UUID.randomUUID()}"
    }
}
