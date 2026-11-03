package co.analiticas.trace.ingestion.domain;

import co.analiticas.trace.shared.domain.DomainException;

/** El archivo no se puede procesar: vacío, dañado o ilegible. */
public class InvalidFileException extends DomainException {

    public InvalidFileException(String message) {
        super("INVALID_FILE", message);
    }
}
