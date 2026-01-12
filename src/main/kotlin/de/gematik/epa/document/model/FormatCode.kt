package de.gematik.epa.document.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Type-safe representation of ePA XDS Format Codes.
 * Based on https://gemspec.gematik.de/ig/fhir/terminology/1.0.7/ValueSet-epa-xds-format-code-vs.html
 * 
 * This sealed interface allows for extensibility while maintaining type safety.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = FormatCode.Standard::class, name = "standard"),
    JsonSubTypes.Type(value = FormatCode.Custom::class, name = "custom")
)
sealed interface FormatCode {
    val code: String
    val system: String
    val display: String
    
    /**
     * Predefined format codes from Gematik terminology
     */
    enum class Standard(
        override val code: String,
        override val system: String = "urn:oid:1.2.276.0.76.3.1.30",
        override val display: String
    ) : FormatCode {
        // IHE PCC formats
        ANTRAG_PFLEGEGRAD("urn:ihe:iti:xds-sd:pdf:2008", "urn:ihe:iti:xds-sd:pdf:2008", "eAU Pflegegutachten PDF"),
        XDS_SD_TEXT("urn:ihe:iti:xds-sd:text:2008", "urn:ihe:iti:xds-sd:text:2008", "Text"),
        
        // Gematik specific formats
        EAU("urn:gematik:ig:eau", "urn:gematik:ig", "eAU"),
        DENTAL_RECORD("urn:gematik:ig:dentalrecord", "urn:gematik:ig", "Dental Record"),
        CHILDS_RECORD("urn:gematik:ig:childsrecord", "urn:gematik:ig", "Child's Record"),
    }
    
    /**
     * Custom format code for runtime-defined formats
     */
    data class Custom(
        override val code: String,
        override val system: String,
        override val display: String
    ) : FormatCode
    
    companion object {
        fun fromCode(code: String): FormatCode? {
            return Standard.entries.firstOrNull { it.code == code }
        }
        
        fun fromCodeOrCustom(code: String, system: String = "urn:gematik:ig", display: String = code): FormatCode {
            return fromCode(code) ?: Custom(code, system, display)
        }
    }
}
