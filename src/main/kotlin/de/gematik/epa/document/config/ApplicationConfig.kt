package de.gematik.epa.document.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.gematik.epa.document.service.StructuredDocumentService
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import org.jboss.logging.Logger

/**
 * Application configuration
 */
@ApplicationScoped
class ApplicationConfig {
    
    @Inject
    lateinit var logger: Logger
    
    @Inject
    lateinit var structuredDocumentService: StructuredDocumentService
    
    /**
     * Produces a configured ObjectMapper
     */
    @Produces
    @ApplicationScoped
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    
    /**
     * Application startup handler
     */
    fun onStart(@Observes event: StartupEvent) {
        logger.info("ePA Structured Documents application starting...")
        
        // Load structured document definitions
        structuredDocumentService.loadDefinitions()
        
        logger.info("ePA Structured Documents application started successfully")
    }
}
