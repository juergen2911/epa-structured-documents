package de.gematik.epa.document.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Type-safe representation of ePA XDS Class Codes.
 * Based on https://gemspec.gematik.de/ig/fhir/terminology/1.0.7/ValueSet-epa-xds-class-code-vs.html
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ClassCode.Standard::class, name = "standard"),
    JsonSubTypes.Type(value = ClassCode.Custom::class, name = "custom")
)
sealed interface ClassCode {
    val code: String
    val system: String
    val display: String
    
    enum class Standard(
        override val code: String,
        override val system: String = "urn:oid:1.2.276.0.76.5.512",
        override val display: String
    ) : ClassCode {
        ADMINISTRATIVE("ADM", "urn:oid:1.2.276.0.76.5.512", "Administrative Dokumente"),
        CLINICAL("CLI", "urn:oid:1.2.276.0.76.5.512", "Klinische Dokumente"),
        DIAGNOSTIC("DIA", "urn:oid:1.2.276.0.76.5.512", "Diagnostische Dokumente"),
        MEDICATION("MED", "urn:oid:1.2.276.0.76.5.512", "Medikationsdokumente"),
        NURSING("NUR", "urn:oid:1.2.276.0.76.5.512", "Pflegedokumente"),
        PREVENTION("PRV", "urn:oid:1.2.276.0.76.5.512", "Präventionsdokumente"),
    }
    
    data class Custom(
        override val code: String,
        override val system: String,
        override val display: String
    ) : ClassCode
    
    companion object {
        fun fromCode(code: String): ClassCode? {
            return Standard.entries.firstOrNull { it.code == code }
        }
        
        fun fromCodeOrCustom(code: String, system: String = "urn:oid:1.2.276.0.76.5.512", display: String = code): ClassCode {
            return fromCode(code) ?: Custom(code, system, display)
        }
    }
}
