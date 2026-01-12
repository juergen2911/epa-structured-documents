package de.gematik.epa.document.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.jboss.logging.Logger
import java.time.LocalDate

/**
 * Structured document definition based on the schema from
 * https://github.com/gematik/ePA-XDS-Document/blob/ePA-3.1.3/src/implementation_guides/ig-schema-definition.json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class StructuredDocumentDefinition(
    val name: String,
    val version: String,
    val description: String? = null,
    
    @JsonProperty("validFrom")
    val validFrom: String? = null,
    
    @JsonProperty("validTo")
    val validTo: String? = null,
    
    @JsonProperty("formatCode")
    val formatCode: CodeDefinition,
    
    @JsonProperty("classCode")
    val classCode: CodeDefinition,
    
    @JsonProperty("typeCode")
    val typeCode: CodeDefinition,
    
    @JsonProperty("mimeType")
    val mimeType: String? = null,
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any>? = null
) {
    companion object {
        private val logger = Logger.getLogger(StructuredDocumentDefinition::class.java)
    }
    
    /**
     * Checks if this definition is valid at a given date
     */
    fun isValidAt(date: LocalDate): Boolean {
        val from = validFrom?.let { parseDate(it) }
        val to = validTo?.let { parseDate(it) }
        
        val afterStart = from?.let { date.isAfter(it) || date.isEqual(it) } ?: true
        val beforeEnd = to?.let { date.isBefore(it) || date.isEqual(it) } ?: true
        
        return afterStart && beforeEnd
    }
    
    private fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString)
        } catch (e: java.time.format.DateTimeParseException) {
            logger.warn("Failed to parse date: $dateString", e)
            null
        }
    }
}

/**
 * Code definition within a structured document
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CodeDefinition(
    val code: String,
    val system: String,
    val display: String? = null
)
