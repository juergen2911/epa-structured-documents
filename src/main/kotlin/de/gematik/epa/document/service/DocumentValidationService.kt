package de.gematik.epa.document.service

import de.gematik.epa.document.model.ClassCode
import de.gematik.epa.document.model.DocumentEntry
import de.gematik.epa.document.model.FormatCode
import de.gematik.epa.document.model.TypeCode
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.LocalDate

/**
 * Service for validating DocumentEntry metadata
 */
@ApplicationScoped
class DocumentValidationService {
    
    @Inject
    lateinit var structuredDocumentService: StructuredDocumentService
    
    @Inject
    lateinit var logger: Logger
    
    /**
     * Validates a document entry
     */
    fun validateDocumentEntry(entry: DocumentEntry, validationDate: LocalDate = LocalDate.now()): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Validate format code
        val formatCodeValidation = validateFormatCode(entry.formatCode, validationDate)
        errors.addAll(formatCodeValidation.errors)
        warnings.addAll(formatCodeValidation.warnings)
        
        // Validate class code
        val classCodeValidation = validateClassCode(entry.classCode)
        errors.addAll(classCodeValidation.errors)
        warnings.addAll(classCodeValidation.warnings)
        
        // Validate type code
        val typeCodeValidation = validateTypeCode(entry.typeCode)
        errors.addAll(typeCodeValidation.errors)
        warnings.addAll(typeCodeValidation.warnings)
        
        // Validate creation time
        if (entry.creationTime.isAfter(validationDate)) {
            errors.add("Creation time ${entry.creationTime} is in the future")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Validates format code against structured document definitions
     */
    fun validateFormatCode(formatCode: FormatCode, validationDate: LocalDate = LocalDate.now()): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check if this is a known standard code
        if (formatCode is FormatCode.Custom) {
            // Check against structured document definitions
            val definition = structuredDocumentService.getDefinitionByFormatCode(formatCode.code)
            if (definition == null) {
                warnings.add("Format code ${formatCode.code} is not a known standard code and has no structured document definition")
            } else {
                // Check if the definition is valid at the given date
                if (!definition.isValidAt(validationDate)) {
                    errors.add("Format code ${formatCode.code} is not valid at date $validationDate (valid from: ${definition.validFrom}, valid to: ${definition.validTo})")
                }
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Validates class code
     */
    fun validateClassCode(classCode: ClassCode): ValidationResult {
        val warnings = mutableListOf<String>()
        
        if (classCode is ClassCode.Custom) {
            warnings.add("Class code ${classCode.code} is not a known standard code")
        }
        
        return ValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = warnings
        )
    }
    
    /**
     * Validates type code
     */
    fun validateTypeCode(typeCode: TypeCode): ValidationResult {
        val warnings = mutableListOf<String>()
        
        if (typeCode is TypeCode.Custom) {
            warnings.add("Type code ${typeCode.code} is not a known standard code")
        }
        
        return ValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = warnings
        )
    }
}

/**
 * Result of a validation operation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)
