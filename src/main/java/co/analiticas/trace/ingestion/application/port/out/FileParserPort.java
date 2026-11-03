package co.analiticas.trace.ingestion.application.port.out;

import co.analiticas.trace.ingestion.domain.RawTable;

/** Estrategia de lectura por formato de archivo (CSV, XLSX, ...). */
public interface FileParserPort {

    boolean supports(String extension);

    RawTable parse(byte[] content);
}
