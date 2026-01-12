package de.gematik.epa.document.model

import java.time.LocalDate

/**
 * Simplified DocumentEntry domain model according to table 30 of gemSpec_Aktensystem_ePAfueralle
 * https://gemspec.gematik.de/docs/gemSpec/gemSpec_Aktensystem_ePAfueralle/gemSpec_Aktensystem_ePAfueralle_V1.7.0/#A_14760-28
 */
data class DocumentEntry(
    val uniqueId: String,
    val formatCode: FormatCode,
    val classCode: ClassCode,
    val typeCode: TypeCode,
    val creationTime: LocalDate,
    val title: String? = null,
    val mimeType: String = "application/pdf",
    val size: Long? = null,
    val hash: String? = null
) {
    /**
     * Validates the document entry against structured document definitions
     */
    fun isValidAt(validationDate: LocalDate): Boolean {
        // Basic validation: creation time should not be in the future
        return !creationTime.isAfter(validationDate)
    }
}
