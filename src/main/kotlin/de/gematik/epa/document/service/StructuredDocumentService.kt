package de.gematik.epa.document.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.gematik.epa.document.model.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.File
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

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
    
    private val definitions = ConcurrentHashMap<String, StructuredDocumentDefinition>()
    
    // Dynamic registries for codes from structured documents
    private val dynamicFormatCodes = ConcurrentHashMap<String, FormatCode>()
    private val dynamicClassCodes = ConcurrentHashMap<String, ClassCode>()
    private val dynamicTypeCodes = ConcurrentHashMap<String, TypeCode>()
    
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
        logger.info("Registered ${dynamicFormatCodes.size} dynamic format codes")
        logger.info("Registered ${dynamicClassCodes.size} dynamic class codes")
        logger.info("Registered ${dynamicTypeCodes.size} dynamic type codes")
    }
    
    private fun loadFromResources() {
        try {
            // Dynamically discover all JSON files in the definitions directory
            val resourcePath = "$definitionsPath/"
            val resourceUrl = this::class.java.classLoader.getResource(resourcePath)
            
            if (resourceUrl != null) {
                // For files packaged in JAR, we need a different approach
                // For now, we'll use a known list but this could be enhanced with classpath scanning
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
                            registerDefinition(definition)
                            logger.info("Loaded definition: ${definition.name} (${definition.version})")
                        } catch (e: Exception) {
                            logger.error("Failed to parse $fileName", e)
                        }
                    } else {
                        logger.warn("Resource not found: $definitionsPath/$fileName")
                    }
                }
            } else {
                logger.warn("Resource directory not found: $definitionsPath")
            }
        } catch (e: Exception) {
            logger.error("Failed to load definitions from resources", e)
        }
    }
    
    private fun loadFromExternalPath() {
        try {
            val externalPath = externalDefinitionsPath.get()
            val externalDir = File(externalPath)
            if (externalDir.exists() && externalDir.isDirectory) {
                externalDir.listFiles { _, name -> name.endsWith(".json") }?.forEach { file ->
                    try {
                        val definition = objectMapper.readValue<StructuredDocumentDefinition>(file)
                        registerDefinition(definition)
                        logger.info("Loaded external definition: ${definition.name} from ${file.name}")
                    } catch (e: Exception) {
                        logger.error("Failed to parse external file ${file.name}", e)
                    }
                }
            } else {
                logger.warn("External definitions path does not exist or is not a directory: $externalPath")
            }
        } catch (e: Exception) {
            logger.error("Failed to load definitions from external path", e)
        }
    }
    
    /**
     * Registers a structured document definition and extends the code types
     */
    private fun registerDefinition(definition: StructuredDocumentDefinition) {
        definitions[definition.formatCode.code] = definition
        
        // Extend FormatCode types dynamically
        val formatCode = FormatCode.Custom(
            code = definition.formatCode.code,
            system = definition.formatCode.system,
            display = definition.formatCode.display ?: definition.formatCode.code
        )
        dynamicFormatCodes[definition.formatCode.code] = formatCode
        
        // Extend ClassCode types dynamically
        val classCode = ClassCode.Custom(
            code = definition.classCode.code,
            system = definition.classCode.system,
            display = definition.classCode.display ?: definition.classCode.code
        )
        dynamicClassCodes[definition.classCode.code] = classCode
        
        // Extend TypeCode types dynamically
        val typeCode = TypeCode.Custom(
            code = definition.typeCode.code,
            system = definition.typeCode.system,
            display = definition.typeCode.display ?: definition.typeCode.code
        )
        dynamicTypeCodes[definition.typeCode.code] = typeCode
    }
    
    /**
     * Gets all loaded definitions
     */
    fun getAllDefinitions(): Collection<StructuredDocumentDefinition> = definitions.values
    
    /**
     * Gets a definition by format code
     */
    fun getDefinitionByFormatCode(formatCode: String): StructuredDocumentDefinition? = definitions[formatCode]
    
    /**
     * Resolves a format code from both standard and dynamic sources
     */
    fun resolveFormatCode(code: String): FormatCode? {
        return FormatCode.fromCode(code) ?: dynamicFormatCodes[code]
    }
    
    /**
     * Resolves a class code from both standard and dynamic sources
     */
    fun resolveClassCode(code: String): ClassCode? {
        return ClassCode.fromCode(code) ?: dynamicClassCodes[code]
    }
    
    /**
     * Resolves a type code from both standard and dynamic sources
     */
    fun resolveTypeCode(code: String): TypeCode? {
        return TypeCode.fromCode(code) ?: dynamicTypeCodes[code]
    }
    
    /**
     * Gets all registered format codes (standard + dynamic)
     */
    fun getAllFormatCodes(): List<FormatCode> {
        return FormatCode.Standard.entries.toList() + dynamicFormatCodes.values
    }
    
    /**
     * Gets all registered class codes (standard + dynamic)
     */
    fun getAllClassCodes(): List<ClassCode> {
        return ClassCode.Standard.entries.toList() + dynamicClassCodes.values
    }
    
    /**
     * Gets all registered type codes (standard + dynamic)
     */
    fun getAllTypeCodes(): List<TypeCode> {
        return TypeCode.Standard.entries.toList() + dynamicTypeCodes.values
    }
}
