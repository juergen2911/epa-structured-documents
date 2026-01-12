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
     * Validates a document entry with two-step validation:
     * 1. Check if formatCode is valid
     * 2. If from structured document, check validationDate and verify classCode/typeCode match
     */
    fun validateDocumentEntry(entry: DocumentEntry, validationDate: LocalDate = LocalDate.now()): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Step 1: Validate format code exists (in standard or dynamic registry)
        val formatCode = structuredDocumentService.resolveFormatCode(entry.formatCode.code)
        if (formatCode == null) {
            errors.add("Format code '${entry.formatCode.code}' is not valid - not found in standard codes or structured document definitions")
            // Cannot continue validation without valid format code
            return ValidationResult(
                isValid = false,
                errors = errors,
                warnings = warnings
            )
        }
        
        // Step 2: Check if format code is from a structured document definition
        val definition = structuredDocumentService.getDefinitionByFormatCode(entry.formatCode.code)
        
        if (definition != null) {
            // This is a structured document, perform additional validation
            logger.debug("Validating DocumentEntry against structured document definition: ${definition.name}")
            
            // Check if the definition is valid at the validation date
            if (!definition.isValidAt(validationDate)) {
                errors.add("Format code '${entry.formatCode.code}' is not valid at date $validationDate (valid from: ${definition.validFrom}, valid to: ${definition.validTo})")
            }
            
            // Verify classCode matches the definition
            if (entry.classCode.code != definition.classCode.code) {
                errors.add("Class code '${entry.classCode.code}' does not match expected value '${definition.classCode.code}' for format code '${entry.formatCode.code}'")
            }
            
            // Verify typeCode matches the definition
            if (entry.typeCode.code != definition.typeCode.code) {
                errors.add("Type code '${entry.typeCode.code}' does not match expected value '${definition.typeCode.code}' for format code '${entry.formatCode.code}'")
            }
            
            // Additional validation: verify class and type codes are registered
            val classCode = structuredDocumentService.resolveClassCode(entry.classCode.code)
            if (classCode == null) {
                warnings.add("Class code '${entry.classCode.code}' is not registered")
            }
            
            val typeCode = structuredDocumentService.resolveTypeCode(entry.typeCode.code)
            if (typeCode == null) {
                warnings.add("Type code '${entry.typeCode.code}' is not registered")
            }
            
        } else {
            // Not a structured document, just verify codes are registered
            logger.debug("Format code '${entry.formatCode.code}' is a standard code, not from structured document definition")
            
            val classCode = structuredDocumentService.resolveClassCode(entry.classCode.code)
            if (classCode == null) {
                errors.add("Class code '${entry.classCode.code}' is not valid - not found in standard codes or structured document definitions")
            }
            
            val typeCode = structuredDocumentService.resolveTypeCode(entry.typeCode.code)
            if (typeCode == null) {
                errors.add("Type code '${entry.typeCode.code}' is not valid - not found in standard codes or structured document definitions")
            }
        }
        
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
        
        // Check if format code is registered (standard or dynamic)
        val resolvedCode = structuredDocumentService.resolveFormatCode(formatCode.code)
        if (resolvedCode == null) {
            errors.add("Format code '${formatCode.code}' is not valid - not found in standard codes or structured document definitions")
            return ValidationResult(
                isValid = false,
                errors = errors,
                warnings = warnings
            )
        }
        
        // Check against structured document definitions
        val definition = structuredDocumentService.getDefinitionByFormatCode(formatCode.code)
        if (definition != null) {
            // Check if the definition is valid at the given date
            if (!definition.isValidAt(validationDate)) {
                errors.add("Format code '${formatCode.code}' is not valid at date $validationDate (valid from: ${definition.validFrom}, valid to: ${definition.validTo})")
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
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        val resolvedCode = structuredDocumentService.resolveClassCode(classCode.code)
        if (resolvedCode == null) {
            errors.add("Class code '${classCode.code}' is not valid - not found in standard codes or structured document definitions")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    /**
     * Validates type code
     */
    fun validateTypeCode(typeCode: TypeCode): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        val resolvedCode = structuredDocumentService.resolveTypeCode(typeCode.code)
        if (resolvedCode == null) {
            errors.add("Type code '${typeCode.code}' is not valid - not found in standard codes or structured document definitions")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
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
