package com.aws.memento.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileStorageService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val uploadDir = Paths.get("upload")

    init {
        try {
            Files.createDirectories(uploadDir)
            logger.info("Upload directory created at: ${uploadDir.toAbsolutePath()}")
        } catch (e: Exception) {
            logger.error("Failed to create upload directory", e)
            throw RuntimeException("Could not create upload directory", e)
        }
    }

    fun store(file: MultipartFile): Pair<String, String> {
        if (file.isEmpty) {
            throw IllegalArgumentException("Failed to store empty file")
        }

        val originalFileName = file.originalFilename ?: "unknown"
        val extension = originalFileName.substringAfterLast(".", "")
        val storedFileName = "${UUID.randomUUID()}.$extension"
        val destinationFile = uploadDir.resolve(storedFileName)

        try {
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING)
            }
            logger.info("File stored: $storedFileName (original: $originalFileName)")
            return Pair(storedFileName, originalFileName)
        } catch (e: Exception) {
            logger.error("Failed to store file: $originalFileName", e)
            throw RuntimeException("Failed to store file", e)
        }
    }

    fun load(fileName: String): Path {
        val file = uploadDir.resolve(fileName)
        if (!Files.exists(file)) {
            throw IllegalArgumentException("File not found: $fileName")
        }
        return file
    }

    fun delete(fileName: String): Boolean {
        return try {
            val file = uploadDir.resolve(fileName)
            Files.deleteIfExists(file)
        } catch (e: Exception) {
            logger.error("Failed to delete file: $fileName", e)
            false
        }
    }

    fun listFiles(): List<String> {
        return try {
            Files.list(uploadDir)
                .filter { Files.isRegularFile(it) }
                .map { it.fileName.toString() }
                .toList()
        } catch (e: Exception) {
            logger.error("Failed to list files", e)
            emptyList()
        }
    }
}
