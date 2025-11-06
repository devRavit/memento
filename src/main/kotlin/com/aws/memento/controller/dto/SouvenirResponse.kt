package com.aws.memento.controller.dto

import com.aws.memento.domain.FeatureType
import com.aws.memento.domain.ProductType
import com.aws.memento.domain.Souvenir
import com.aws.memento.domain.SouvenirStatus
import com.aws.memento.domain.SouvenirStyle
import java.time.Instant

data class SouvenirResponse(
    val id: String,
    val userId: String,
    val status: SouvenirStatus,
    val inputImageUrls: List<String>,
    val style: SouvenirStyle?,
    val productType: ProductType?,
    val analysis: ImageAnalysisResponse?,
    val generatedImageUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ImageAnalysisResponse(
    val features: List<ImageFeatureResponse>,
    val suggestedStyle: SouvenirStyle,
    val suggestedProductType: ProductType,
    val confidence: Double,
)

data class ImageFeatureResponse(
    val type: FeatureType,
    val description: String,
    val importance: Double,
)

fun Souvenir.toResponse(): SouvenirResponse {
    return SouvenirResponse(
        id = id,
        userId = userId,
        status = status,
        inputImageUrls = inputImageUrls,
        style = style,
        productType = productType,
        analysis =
            analysis?.let {
                ImageAnalysisResponse(
                    features =
                        it.features.map { feature ->
                            ImageFeatureResponse(
                                type = feature.type,
                                description = feature.description,
                                importance = feature.importance,
                            )
                        },
                    suggestedStyle = it.suggestedStyle,
                    suggestedProductType = it.suggestedProductType,
                    confidence = it.confidence,
                )
            },
        generatedImageUrl = generatedImageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
