package de.gematik.epa.document.resource

import de.gematik.epa.document.model.ClassCode
import de.gematik.epa.document.model.DocumentEntry
import de.gematik.epa.document.model.FormatCode
import de.gematik.epa.document.model.TypeCode
import de.gematik.epa.document.service.DocumentValidationService
import de.gematik.epa.document.service.ValidationResult
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger
import java.time.LocalDate

/**
 * REST API for document entry validation
 */
@Path("/api/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class DocumentEntryResource {
    
    @Inject
    lateinit var validationService: DocumentValidationService
    
    @Inject
    lateinit var logger: Logger
    
    /**
     * Validates a document entry
     */
    @POST
    @Path("/validate")
    fun validateDocument(request: DocumentValidationRequest): Response {
        logger.info("Validating document entry: ${request.documentEntry.uniqueId}")
        
        val validationDate = request.validationDate ?: LocalDate.now()
        val result = validationService.validateDocumentEntry(request.documentEntry, validationDate)
        
        return if (result.isValid) {
            Response.ok(result).build()
        } else {
            Response.status(Response.Status.BAD_REQUEST).entity(result).build()
        }
    }
    
    /**
     * Validates format code
     */
    @POST
    @Path("/validate/format-code")
    fun validateFormatCode(request: FormatCodeValidationRequest): Response {
        val formatCode = FormatCode.fromCodeOrCustom(
            request.code,
            request.system ?: "urn:gematik:ig",
            request.display ?: request.code
        )
        
        val validationDate = request.validationDate ?: LocalDate.now()
        val result = validationService.validateFormatCode(formatCode, validationDate)
        
        return Response.ok(result).build()
    }
    
    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    fun health(): Response {
        return Response.ok(mapOf("status" to "UP")).build()
    }
}

/**
 * Request for document validation
 */
data class DocumentValidationRequest(
    val documentEntry: DocumentEntry,
    val validationDate: LocalDate? = null
)

/**
 * Request for format code validation
 */
data class FormatCodeValidationRequest(
    val code: String,
    val system: String? = null,
    val display: String? = null,
    val validationDate: LocalDate? = null
)
