# ePA Structured Documents

Framework for structured document validation in the German electronic patient record (ePA) system.

## Overview

This Quarkus-based application provides REST APIs to validate DocumentEntry metadata objects against structured document definitions from Gematik.

## Features

- **Type-safe Code Types**: Extensible sealed interfaces for FormatCode, ClassCode, and TypeCode
- **Structured Document Validation**: Validates documents against official Gematik implementation guides
- **Date-based Validation**: Checks if format codes are valid at specific dates
- **Dynamic Configuration**: Supports loading custom document definitions at runtime
- **REST API**: Simple HTTP endpoints for document validation

## Building

```bash
./gradlew build
```

## Running

### Development Mode

```bash
./gradlew quarkusDev
```

The application will start on http://localhost:8080

### Production Mode

```bash
java -jar build/quarkus-app/quarkus-run.jar
```

## API Endpoints

### Health Check

```bash
curl http://localhost:8080/api/documents/health
```

### Validate Document Entry

```bash
curl -X POST http://localhost:8080/api/documents/validate \
  -H "Content-Type: application/json" \
  -d '{
    "documentEntry": {
      "uniqueId": "1.2.3.4.5",
      "formatCode": {
        "type": "standard",
        "code": "urn:gematik:ig:eau",
        "system": "urn:gematik:ig",
        "display": "eAU"
      },
      "classCode": {
        "type": "standard",
        "code": "ADM",
        "system": "urn:oid:1.2.276.0.76.5.512",
        "display": "Administrative Dokumente"
      },
      "typeCode": {
        "type": "standard",
        "code": "11488-4",
        "system": "http://loinc.org",
        "display": "Consultation note"
      },
      "creationTime": "2024-01-15",
      "title": "Test Document"
    },
    "validationDate": "2024-01-20"
  }'
```

### Validate Format Code

```bash
curl -X POST http://localhost:8080/api/documents/validate/format-code \
  -H "Content-Type: application/json" \
  -d '{
    "code": "urn:gematik:ig:eau:v1.2",
    "system": "urn:gematik:ig",
    "display": "eAU v1.2",
    "validationDate": "2024-06-01"
  }'
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# HTTP Server
quarkus.http.port=8080

# Structured document definitions
epa.documents.definitions.path=structured-documents
# epa.documents.definitions.external=/path/to/external/definitions
```

## Structured Document Definitions

The application includes sample definitions based on Gematik specifications:

- `ig-eau_V_1_2.json` - eAU (electronic sick note) v1.2
- `ig-eau_V_1_1.json` - eAU v1.1 (deprecated)
- `ig-dentalrecord.json` - Dental Record
- `ig-childsrecord_V1_0_1.json` - Child's Health Record

### Adding Custom Definitions

1. Create JSON files following the schema
2. Place them in `src/main/resources/structured-documents/`
3. Or configure an external directory via `epa.documents.definitions.external`

## References

- [ePA Specification](https://gemspec.gematik.de/docs/gemSpec/gemSpec_Aktensystem_ePAfueralle/)
- [Format Code ValueSet](https://gemspec.gematik.de/ig/fhir/terminology/1.0.7/ValueSet-epa-xds-format-code-vs.html)
- [Class Code ValueSet](https://gemspec.gematik.de/ig/fhir/terminology/1.0.7/ValueSet-epa-xds-class-code-vs.html)
- [Type Code ValueSet](https://gemspec.gematik.de/ig/fhir/terminology/1.0.7/ValueSet-epa-xds-type-code-vs.html)
- [Gematik Document Repository](https://github.com/gematik/ePA-XDS-Document)

