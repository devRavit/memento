package com.aws.memento.controller

import com.aws.memento.controller.dto.FileUploadResponse
import com.aws.memento.service.FileStorageService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileStorageService: FileStorageService,
) {
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<FileUploadResponse> {
        return try {
            val (storedFileName, originalFileName) = fileStorageService.store(file)

            val response =
                FileUploadResponse(
                    fileName = storedFileName,
                    originalFileName = originalFileName,
                    fileSize = file.size,
                    contentType = file.contentType,
                    uploadPath = "/api/v1/files/download/$storedFileName",
                )

            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/upload-multiple", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadMultipleFiles(
        @RequestParam("files") files: Array<MultipartFile>,
    ): ResponseEntity<List<FileUploadResponse>> {
        return try {
            val responses =
                files.map { file ->
                    val (storedFileName, originalFileName) = fileStorageService.store(file)

                    FileUploadResponse(
                        fileName = storedFileName,
                        originalFileName = originalFileName,
                        fileSize = file.size,
                        contentType = file.contentType,
                        uploadPath = "/api/v1/files/download/$storedFileName",
                    )
                }

            ResponseEntity.ok(responses)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/download/{fileName}")
    fun downloadFile(
        @PathVariable fileName: String,
    ): ResponseEntity<Resource> {
        return try {
            val file =
                try {
                    fileStorageService.load(fileName)
                } catch (e: IllegalArgumentException) {
                    fileStorageService.loadGeneratedImage(fileName)
                }
            val resource = FileSystemResource(file)

            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.fileName}\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/list")
    fun listFiles(): ResponseEntity<Map<String, Any>> {
        val files = fileStorageService.listFiles()
        return ResponseEntity.ok(
            mapOf(
                "files" to files,
                "count" to files.size,
            ),
        )
    }

    @DeleteMapping("/{fileName}")
    fun deleteFile(
        @PathVariable fileName: String,
    ): ResponseEntity<Map<String, String>> {
        return if (fileStorageService.delete(fileName)) {
            ResponseEntity.ok(mapOf("message" to "File deleted successfully"))
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Failed to delete file"))
        }
    }
}
