package com.procurement.storage.infrastructure.handler.open

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.storage.application.service.StorageService
import com.procurement.storage.domain.fail.Fail
import com.procurement.storage.domain.fail.error.BadRequestErrors
import com.procurement.storage.domain.util.Result
import com.procurement.storage.infrastructure.dto.OpenAccessResult
import com.procurement.storage.infrastructure.dto.converter.convert
import com.procurement.storage.infrastructure.handler.AbstractQueryHandler
import com.procurement.storage.model.dto.bpe.Command2Type
import com.procurement.storage.model.dto.bpe.tryGetParams
import com.procurement.storage.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class OpenAccessHandler(
    private val storageService: StorageService
) : AbstractQueryHandler<Command2Type, List<OpenAccessResult>>() {

    override fun execute(node: JsonNode): Result<List<OpenAccessResult>, Fail> {
        val paramsNode = node.tryGetParams()
            .doOnError { error -> return Result.failure(error) }
            .get

        val params = paramsNode.tryToObject(OpenAccessRequest::class.java)
            .doOnError { error ->
                return Result.failure(
                    BadRequestErrors.Parsing(
                        message = "Can not parse to ${error.className}",
                        request = paramsNode.toString()
                    )
                )
            }
            .get
            .convert()
            .doOnError { error -> return Result.failure(error) }
            .get

        return storageService.openAccess(requestDocumentIds = params.documentIds)
    }

    override val action: Command2Type
        get() = Command2Type.OPEN_ACCESS
}
