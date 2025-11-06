package com.aws.memento.domain

import java.time.Instant

data class Souvenir(
    val id: String,
    val userId: String,
    val status: SouvenirStatus,
    val inputImageUrls: List<String>,
    val style: SouvenirStyle?,
    val productType: ProductType?,
    val analysis: ImageAnalysis?,
    val generatedImageUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class SouvenirStatus {
    UPLOADING,
    ANALYZING,
    GENERATING,
    COMPLETED,
    FAILED,
}

enum class SouvenirStyle {
    ANIMATION,
    REALISTIC,
    CARTOON,
    WATERCOLOR,
    OIL_PAINTING,
    SKETCH,
}

enum class ProductType {
    CALENDAR,
    PHOTOBOOK,
    POSTER,
    POSTCARD,
    THREE_D_PRINT,
}
