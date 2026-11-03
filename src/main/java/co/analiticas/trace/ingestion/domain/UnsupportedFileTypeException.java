package co.analiticas.trace.ingestion.domain;

import co.analiticas.trace.shared.domain.DomainException;

public class UnsupportedFileTypeException extends DomainException {

    public UnsupportedFileTypeException(String fileName) {
        super("UNSUPPORTED_FILE_TYPE",
                "Tipo de archivo no soportado: '" + fileName + "'. Solo se aceptan archivos .csv o .xlsx.");
    }
}
