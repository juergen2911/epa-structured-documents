package de.gematik.epa.document

import de.gematik.epa.document.model.ClassCode
import de.gematik.epa.document.model.DocumentEntry
import de.gematik.epa.document.model.FormatCode
import de.gematik.epa.document.model.TypeCode
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import java.time.LocalDate

@QuarkusTest
class DocumentEntryResourceTest {
    
    @Test
    fun testHealthEndpoint() {
        given()
            .`when`().get("/api/documents/health")
            .then()
            .statusCode(200)
            .body("status", `is`("UP"))
    }
    
    @Test
    fun testValidateDocumentWithStandardCodes() {
        // Test with standard codes that don't have a structured document definition
        val validDocument = DocumentEntry(
            uniqueId = "1.2.3.4.5",
            formatCode = FormatCode.Standard.EAU,
            classCode = ClassCode.Standard.ADMINISTRATIVE,
            typeCode = TypeCode.Standard.CONSULTATION_NOTE,
            creationTime = LocalDate.now().minusDays(1),
            title = "Test Document"
        )
        
        val request = mapOf(
            "documentEntry" to validDocument
        )
        
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .`when`().post("/api/documents/validate")
            .then()
            .statusCode(200)
            .body("isValid", `is`(true))
    }
    
    @Test
    fun testValidateDocumentFromStructuredDefinition() {
        // Create a document entry matching the ig-eau_V_1_2 structured document definition
        // formatCode: urn:gematik:ig:eau:v1.2, classCode: ADM, typeCode: 11488-4
        val structuredDocument = DocumentEntry(
            uniqueId = "1.2.3.4.6",
            formatCode = FormatCode.Custom("urn:gematik:ig:eau:v1.2", "urn:gematik:ig", "eAU v1.2"),
            classCode = ClassCode.Custom("ADM", "urn:oid:1.2.276.0.76.5.512", "Administrative Dokumente"),
            typeCode = TypeCode.Custom("11488-4", "http://loinc.org", "Consultation note"),
            creationTime = LocalDate.of(2024, 6, 1),
            title = "eAU Test Document"
        )
        
        val request = mapOf(
            "documentEntry" to structuredDocument,
            "validationDate" to "2024-06-15"
        )
        
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .`when`().post("/api/documents/validate")
            .then()
            .statusCode(200)
            .body("isValid", `is`(true))
    }
    
    @Test
    fun testValidateDocumentWithMismatchedClassCode() {
        // Create a document with wrong classCode for the structured document
        val invalidDocument = DocumentEntry(
            uniqueId = "1.2.3.4.7",
            formatCode = FormatCode.Custom("urn:gematik:ig:eau:v1.2", "urn:gematik:ig", "eAU v1.2"),
            classCode = ClassCode.Custom("CLI", "urn:oid:1.2.276.0.76.5.512", "Klinische Dokumente"),
            typeCode = TypeCode.Custom("11488-4", "http://loinc.org", "Consultation note"),
            creationTime = LocalDate.of(2024, 6, 1),
            title = "Invalid eAU Document"
        )
        
        val request = mapOf(
            "documentEntry" to invalidDocument,
            "validationDate" to "2024-06-15"
        )
        
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .`when`().post("/api/documents/validate")
            .then()
            .statusCode(400)
            .body("isValid", `is`(false))
    }
}
