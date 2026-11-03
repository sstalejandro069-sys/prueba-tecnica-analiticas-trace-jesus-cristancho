package co.analiticas.trace.ingestion.domain;

import java.util.List;
import java.util.Map;

/**
 * Contenido de un archivo sin interpretar: lo que entregan los parsers.
 * Las reglas de negocio se aplican después, en el dominio.
 */
public record RawTable(List<String> headers, List<RawRow> rows) {

    /** @param rowNumber número de fila tal como lo ve el usuario en el archivo (el encabezado es la fila 1). */
    public record RawRow(int rowNumber, Map<String, String> values) {
    }
}
