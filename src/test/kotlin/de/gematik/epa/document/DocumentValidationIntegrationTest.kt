package de.gematik.epa.document

import de.gematik.epa.document.model.ClassCode
import de.gematik.epa.document.model.DocumentEntry
import de.gematik.epa.document.model.FormatCode
import de.gematik.epa.document.model.TypeCode
import de.gematik.epa.document.service.DocumentValidationService
import de.gematik.epa.document.service.StructuredDocumentService
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

@QuarkusTest
class DocumentValidationIntegrationTest {
    
    @Inject
    lateinit var validationService: DocumentValidationService
    
    @Inject
    lateinit var structuredDocumentService: StructuredDocumentService
    
    @Test
    fun testDynamicCodeRegistration() {
        // Verify that codes from structured documents are registered
        val formatCodes = structuredDocumentService.getAllFormatCodes()
        assertTrue(formatCodes.isNotEmpty(), "Should have registered format codes")
        
        val classCodes = structuredDocumentService.getAllClassCodes()
        assertTrue(classCodes.isNotEmpty(), "Should have registered class codes")
        
        val typeCodes = structuredDocumentService.getAllTypeCodes()
        assertTrue(typeCodes.isNotEmpty(), "Should have registered type codes")
        
        // Verify specific codes from structured documents are registered
        val eauFormatCode = structuredDocumentService.resolveFormatCode("urn:gematik:ig:eau:v1.2")
        assertNotNull(eauFormatCode, "eAU v1.2 format code should be registered")
        
        val admClassCode = structuredDocumentService.resolveClassCode("ADM")
        assertNotNull(admClassCode, "ADM class code should be registered")
    }
    
    @Test
    fun testValidDocumentFromStructuredDefinition() {
        // Create a valid document matching ig-eau_V_1_2 definition
        val validDocument = DocumentEntry(
            uniqueId = "1.2.3.4.5",
            formatCode = FormatCode.Custom("urn:gematik:ig:eau:v1.2", "urn:gematik:ig", "eAU v1.2"),
            classCode = ClassCode.Custom("ADM", "urn:oid:1.2.276.0.76.5.512", "Administrative Dokumente"),
            typeCode = TypeCode.Custom("11488-4", "http://loinc.org", "Consultation note"),
            creationTime = LocalDate.of(2024, 6, 1),
            title = "Valid eAU Document"
        )
        
        val result = validationService.validateDocumentEntry(validDocument, LocalDate.of(2024, 6, 15))
        
        assertTrue(result.isValid, "Document should be valid")
        assertTrue(result.errors.isEmpty(), "Should have no errors")
    }
    
    @Test
    fun testInvalidDateForStructuredDefinition() {
        // Test with eAU v1.1 which is only valid until 2023-12-31
        val documentWithExpiredCode = DocumentEntry(
            uniqueId = "1.2.3.4.6",
            formatCode = FormatCode.Custom("urn:gematik:ig:eau:v1.1", "urn:gematik:ig", "eAU v1.1"),
            classCode = ClassCode.Custom("ADM", "urn:oid:1.2.276.0.76.5.512", "Administrative Dokumente"),
            typeCode = TypeCode.Custom("11488-4", "http://loinc.org", "Consultation note"),
            creationTime = LocalDate.of(2024, 1, 10),
            title = "Document with expired format code"
        )
        
        val result = validationService.validateDocumentEntry(documentWithExpiredCode, LocalDate.of(2024, 6, 15))
        
        assertFalse(result.isValid, "Document should be invalid due to expired format code")
        assertTrue(result.errors.any { it.contains("not valid at date") }, "Should have date validation error")
    }
    
    @Test
    fun testMismatchedClassCode() {
        // Create document with wrong class code for the structured definition
        val documentWithWrongClass = DocumentEntry(
            uniqueId = "1.2.3.4.7",
            formatCode = FormatCode.Custom("urn:gematik:ig:eau:v1.2", "urn:gematik:ig", "eAU v1.2"),
            classCode = ClassCode.Custom("CLI", "urn:oid:1.2.276.0.76.5.512", "Klinische Dokumente"),
            typeCode = TypeCode.Custom("11488-4", "http://loinc.org", "Consultation note"),
            creationTime = LocalDate.of(2024, 6, 1),
            title = "Document with wrong class code"
        )
        
        val result = validationService.validateDocumentEntry(documentWithWrongClass, LocalDate.of(2024, 6, 15))
        
        assertFalse(result.isValid, "Document should be invalid due to mismatched class code")
        assertTrue(result.errors.any { it.contains("does not match expected value") }, "Should have class code mismatch error")
    }
    
    @Test
    fun testMismatchedTypeCode() {
        // Create document with wrong type code for the structured definition
        val documentWithWrongType = DocumentEntry(
            uniqueId = "1.2.3.4.8",
            formatCode = FormatCode.Custom("urn:gematik:ig:eau:v1.2", "urn:gematik:ig", "eAU v1.2"),
            classCode = ClassCode.Custom("ADM", "urn:oid:1.2.276.0.76.5.512", "Administrative Dokumente"),
            typeCode = TypeCode.Custom("34117-2", "http://loinc.org", "History and physical note"),
            creationTime = LocalDate.of(2024, 6, 1),
            title = "Document with wrong type code"
        )
        
        val result = validationService.validateDocumentEntry(documentWithWrongType, LocalDate.of(2024, 6, 15))
        
        assertFalse(result.isValid, "Document should be invalid due to mismatched type code")
        assertTrue(result.errors.any { it.contains("does not match expected value") }, "Should have type code mismatch error")
    }
    
    @Test
    fun testUnknownFormatCode() {
        // Create document with unknown format code
        val documentWithUnknownCode = DocumentEntry(
            uniqueId = "1.2.3.4.9",
            formatCode = FormatCode.Custom("urn:unknown:format", "urn:unknown", "Unknown Format"),
            classCode = ClassCode.Custom("ADM", "urn:oid:1.2.276.0.76.5.512", "Administrative Dokumente"),
            typeCode = TypeCode.Custom("11488-4", "http://loinc.org", "Consultation note"),
            creationTime = LocalDate.of(2024, 6, 1),
            title = "Document with unknown format code"
        )
        
        val result = validationService.validateDocumentEntry(documentWithUnknownCode, LocalDate.of(2024, 6, 15))
        
        assertFalse(result.isValid, "Document should be invalid due to unknown format code")
        assertTrue(result.errors.any { it.contains("not valid - not found") }, "Should have unknown format code error")
    }
    
    @Test
    fun testStandardCodeWithoutStructuredDefinition() {
        // Use a standard code that doesn't have a structured document definition
        val standardDocument = DocumentEntry(
            uniqueId = "1.2.3.4.10",
            formatCode = FormatCode.Standard.EAU,
            classCode = ClassCode.Standard.ADMINISTRATIVE,
            typeCode = TypeCode.Standard.CONSULTATION_NOTE,
            creationTime = LocalDate.of(2024, 6, 1),
            title = "Document with standard codes"
        )
        
        val result = validationService.validateDocumentEntry(standardDocument, LocalDate.of(2024, 6, 15))
        
        assertTrue(result.isValid, "Document with standard codes should be valid")
    }
}
