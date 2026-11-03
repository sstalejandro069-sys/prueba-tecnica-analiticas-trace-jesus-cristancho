package co.analiticas.trace.ingestion.domain;

import co.analiticas.trace.shared.domain.DomainException;

import java.util.List;

/** El archivo se leyó, pero su contenido no cumple el esquema esperado. */
public class InvalidFileContentException extends DomainException {

    public InvalidFileContentException(String message, List<String> details) {
        super("INVALID_FILE_CONTENT", message, details);
    }
}
