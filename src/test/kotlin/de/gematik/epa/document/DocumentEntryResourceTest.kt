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
    fun testValidateValidDocument() {
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
}
