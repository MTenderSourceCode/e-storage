package com.procurement.storage.infrastructure.handler.check.registration

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.storage.application.service.StorageService
import com.procurement.storage.domain.fail.Fail
import com.procurement.storage.domain.fail.error.BadRequestErrors
import com.procurement.storage.domain.util.ValidationResult
import com.procurement.storage.infrastructure.dto.converter.convert
import com.procurement.storage.infrastructure.handler.AbstractValidationHandler
import com.procurement.storage.model.dto.bpe.Command2Type
import com.procurement.storage.model.dto.bpe.tryGetParams
import com.procurement.storage.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class CheckRegistrationHandler(
    private val storageService: StorageService
) : AbstractValidationHandler<Command2Type>() {

    override fun execute(node: JsonNode): ValidationResult<Fail> {

        val paramsNode = node.tryGetParams()
            .doOnError { error -> return ValidationResult.error(error) }
            .get

        val params = paramsNode.tryToObject(CheckRegistrationRequest::class.java)
            .doOnError { error ->
                return ValidationResult.error(
                    BadRequestErrors.Parsing(
                        message = "Can not parse to ${error.className}",
                        request = paramsNode.toString()
                    )
                )
            }
            .get
            .convert()
            .doOnError { error -> return ValidationResult.error(error) }
            .get

        val serviceResult = storageService.checkRegistration(requestDocumentIds = params.documentIds)
        if (serviceResult.isError)
            return ValidationResult.error(serviceResult.error)

        return ValidationResult.ok()
    }

    override val action: Command2Type
        get() = Command2Type.CHECK_REGISTRATION
}
