package com.aws.memento.domain

data class ImageAnalysis(
    val features: List<ImageFeature>,
    val suggestedStyle: SouvenirStyle,
    val suggestedProductType: ProductType,
    val novaCanvasPrompt: String,
    val confidence: Double,
)

data class ImageFeature(
    val type: FeatureType,
    val description: String,
    val importance: Double,
)

enum class FeatureType {
    PERSON,
    BACKGROUND,
    LANDSCAPE,
    OBJECT,
    MOOD,
    COLOR_TONE,
}
