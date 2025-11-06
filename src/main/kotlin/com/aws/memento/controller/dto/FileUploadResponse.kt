package com.aws.memento.controller.dto

data class FileUploadResponse(
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val contentType: String?,
    val uploadPath: String,
)
