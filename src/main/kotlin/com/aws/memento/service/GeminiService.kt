package com.aws.memento.service

import com.aws.memento.domain.FeatureType
import com.aws.memento.domain.ImageAnalysis
import com.aws.memento.domain.ImageFeature
import com.aws.memento.domain.ProductType
import com.aws.memento.domain.SouvenirStyle
import org.springframework.stereotype.Service

interface GeminiService {
    fun analyzeImages(
        imageUrls: List<String>,
        preferredStyle: SouvenirStyle?,
        preferredProductType: ProductType?,
    ): ImageAnalysis
}

@Service
class GeminiServiceImpl : GeminiService {
    override fun analyzeImages(
        imageUrls: List<String>,
        preferredStyle: SouvenirStyle?,
        preferredProductType: ProductType?,
    ): ImageAnalysis {
        val features =
            listOf(
                ImageFeature(
                    type = FeatureType.PERSON,
                    description = "Two people smiling at the beach",
                    importance = 0.9,
                ),
                ImageFeature(
                    type = FeatureType.BACKGROUND,
                    description = "Beautiful sunset with ocean view",
                    importance = 0.8,
                ),
                ImageFeature(
                    type = FeatureType.MOOD,
                    description = "Happy and romantic atmosphere",
                    importance = 0.7,
                ),
            )

        val suggestedStyle = preferredStyle ?: SouvenirStyle.REALISTIC
        val suggestedProductType = preferredProductType ?: ProductType.POSTER

        val prompt = buildNovaCanvasPrompt(features, suggestedStyle, suggestedProductType)

        return ImageAnalysis(
            features = features,
            suggestedStyle = suggestedStyle,
            suggestedProductType = suggestedProductType,
            novaCanvasPrompt = prompt,
            confidence = 0.85,
        )
    }

    private fun buildNovaCanvasPrompt(
        features: List<ImageFeature>,
        style: SouvenirStyle,
        productType: ProductType,
    ): String {
        val featureDescriptions = features.joinToString(", ") { it.description }
        val styleKeyword =
            when (style) {
                SouvenirStyle.ANIMATION -> "anime style, vibrant colors"
                SouvenirStyle.REALISTIC -> "photorealistic, high quality"
                SouvenirStyle.CARTOON -> "cartoon style, playful"
                SouvenirStyle.WATERCOLOR -> "watercolor painting style"
                SouvenirStyle.OIL_PAINTING -> "oil painting style, classical"
                SouvenirStyle.SKETCH -> "pencil sketch style"
            }

        return "Create a $styleKeyword image for a $productType featuring: $featureDescriptions"
    }
}
