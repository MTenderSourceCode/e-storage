package com.procurement.storage.controller

import com.procurement.storage.model.dto.registration.DocumentsRequestDto
import com.procurement.storage.model.dto.registration.RegistrationRequestDto
import com.procurement.storage.service.StorageService
import org.springframework.core.io.Resource
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping(value = ["/storage"])
class StorageController(private val storageService: StorageService) {

    @PostMapping(value = ["/registration"])
    fun makeRegistration(@RequestBody dto: RegistrationRequestDto) =
            ResponseEntity(storageService.registerFile(dto), HttpStatus.OK)


    @PostMapping(value = ["/upload/{fileId}"], consumes = ["multipart/form-data"])
    fun uploadFile(@PathVariable(value = "fileId") fileId: String,
                   @RequestParam(value = "file") file: MultipartFile) =
            ResponseEntity(storageService.uploadFile(fileId, file), HttpStatus.CREATED)

    @PostMapping(value = ["/publish"])
    fun setPublishDate(@RequestParam(value = "datePublished")
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                       datePublished: LocalDateTime,
                       @RequestBody dto: DocumentsRequestDto) =
            ResponseEntity(storageService.setPublishDateBatch(datePublished, dto), HttpStatus.OK)

    @GetMapping(value = ["/get/{fileId}"])
    fun getFile(@PathVariable(value = "fileId") fileId: String): ResponseEntity<Resource> {
        val file = storageService.getFileById(fileId)
        val resource = file.resource
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("application/octet-stream")
        headers.contentDisposition = ContentDisposition.parse("attachment; filename=" + file.fileName)
        headers.contentLength = resource.contentLength()
        return ResponseEntity(resource, headers, HttpStatus.OK)
    }
}