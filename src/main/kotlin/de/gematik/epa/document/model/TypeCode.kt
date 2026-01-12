package de.gematik.epa.document.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Type-safe representation of ePA XDS Type Codes.
 * Based on https://gemspec.gematik.de/ig/fhir/terminology/1.0.7/ValueSet-epa-xds-type-code-vs.html
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TypeCode.Standard::class, name = "standard"),
    JsonSubTypes.Type(value = TypeCode.Custom::class, name = "custom")
)
sealed interface TypeCode {
    val code: String
    val system: String
    val display: String
    
    enum class Standard(
        override val code: String,
        override val system: String = "http://loinc.org",
        override val display: String
    ) : TypeCode {
        // LOINC codes for common document types
        DISCHARGE_SUMMARY("18842-5", "http://loinc.org", "Discharge summary"),
        CONSULTATION_NOTE("11488-4", "http://loinc.org", "Consultation note"),
        PROGRESS_NOTE("11506-3", "http://loinc.org", "Progress note"),
        HISTORY_AND_PHYSICAL("34117-2", "http://loinc.org", "History and physical note"),
        OPERATIVE_NOTE("11504-8", "http://loinc.org", "Operative note"),
        PROCEDURE_NOTE("28570-0", "http://loinc.org", "Procedure note"),
        MEDICATION_LIST("56445-0", "http://loinc.org", "Medication summary"),
    }
    
    data class Custom(
        override val code: String,
        override val system: String,
        override val display: String
    ) : TypeCode
    
    companion object {
        fun fromCode(code: String): TypeCode? {
            return Standard.entries.firstOrNull { it.code == code }
        }
        
        fun fromCodeOrCustom(code: String, system: String = "http://loinc.org", display: String = code): TypeCode {
            return fromCode(code) ?: Custom(code, system, display)
        }
    }
}
