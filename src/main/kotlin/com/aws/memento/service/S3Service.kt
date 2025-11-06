package com.aws.memento.service

import org.springframework.stereotype.Service

interface S3Service {
    fun uploadImage(
        souvenirId: String,
        fileName: String,
        base64Data: String,
    ): String

    fun getImageUrl(key: String): String
}

@Service
class S3ServiceImpl : S3Service {
    override fun uploadImage(
        souvenirId: String,
        fileName: String,
        base64Data: String,
    ): String {
        val s3Key = "souvenirs/$souvenirId/$fileName"

        return "https://memento-bucket.s3.amazonaws.com/$s3Key"
    }

    override fun getImageUrl(key: String): String {
        return "https://memento-bucket.s3.amazonaws.com/$key"
    }
}
