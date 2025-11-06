package com.aws.memento.controller.dto

import com.aws.memento.domain.ProductType
import com.aws.memento.domain.SouvenirStyle

data class CreateSouvenirRequest(
    val userId: String,
    val images: List<ImageUpload>,
    val preferredStyle: SouvenirStyle?,
    val preferredProductType: ProductType?,
)

data class ImageUpload(
    val fileName: String,
    val contentType: String,
    val base64Data: String,
)
