package de.gematik.epa.document.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.gematik.epa.document.model.StructuredDocumentDefinition
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.File
import java.util.Optional

/**
 * Service for loading and managing structured document definitions
 */
@ApplicationScoped
class StructuredDocumentService {
    
    @Inject
    lateinit var objectMapper: ObjectMapper
    
    @Inject
    lateinit var logger: Logger
    
    @ConfigProperty(name = "epa.documents.definitions.path", defaultValue = "structured-documents")
    lateinit var definitionsPath: String
    
    @ConfigProperty(name = "epa.documents.definitions.external")
    lateinit var externalDefinitionsPath: Optional<String>
    
    private val definitions = mutableMapOf<String, StructuredDocumentDefinition>()
    
    /**
     * Loads structured document definitions from resources and external paths
     */
    fun loadDefinitions() {
        logger.info("Loading structured document definitions...")
        
        // Load from classpath resources
        loadFromResources()
        
        // Load from external path if configured
        if (externalDefinitionsPath.isPresent) {
            loadFromExternalPath()
        }
        
        logger.info("Loaded ${definitions.size} structured document definitions")
    }
    
    private fun loadFromResources() {
        try {
            val resourceFiles = listOf(
                "ig-eau_V_1_2.json",
                "ig-eau_V_1_1.json",
                "ig-dentalrecord.json",
                "ig-childsrecord_V1_0_1.json"
            )
            
            resourceFiles.forEach { fileName ->
                val resourceStream = this::class.java.classLoader.getResourceAsStream("$definitionsPath/$fileName")
                if (resourceStream != null) {
                    try {
                        val definition = objectMapper.readValue<StructuredDocumentDefinition>(resourceStream)
                        definitions[definition.formatCode.code] = definition
                        logger.info("Loaded definition: ${definition.name} (${definition.version})")
                    } catch (e: Exception) {
                        logger.error("Failed to parse $fileName", e)
                    }
                } else {
                    logger.warn("Resource not found: $definitionsPath/$fileName")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to load definitions from resources", e)
        }
    }
    
    private fun loadFromExternalPath() {
        try {
            val externalDir = File(externalDefinitionsPath.get())
            if (externalDir.exists() && externalDir.isDirectory) {
                externalDir.listFiles { _, name -> name.endsWith(".json") }?.forEach { file ->
                    try {
                        val definition = objectMapper.readValue<StructuredDocumentDefinition>(file)
                        definitions[definition.formatCode.code] = definition
                        logger.info("Loaded external definition: ${definition.name} from ${file.name}")
                    } catch (e: Exception) {
                        logger.error("Failed to parse external file ${file.name}", e)
                    }
                }
            } else {
                logger.warn("External definitions path does not exist or is not a directory: $externalDefinitionsPath")
            }
        } catch (e: Exception) {
            logger.error("Failed to load definitions from external path", e)
        }
    }
    
    /**
     * Gets all loaded definitions
     */
    fun getAllDefinitions(): Collection<StructuredDocumentDefinition> = definitions.values
    
    /**
     * Gets a definition by format code
     */
    fun getDefinitionByFormatCode(formatCode: String): StructuredDocumentDefinition? = definitions[formatCode]
}
