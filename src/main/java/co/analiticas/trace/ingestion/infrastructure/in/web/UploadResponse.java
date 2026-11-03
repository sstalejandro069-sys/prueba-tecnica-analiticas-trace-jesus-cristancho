package co.analiticas.trace.ingestion.infrastructure.in.web;

import co.analiticas.trace.ingestion.domain.IngestionResult;
import co.analiticas.trace.ingestion.domain.RowError;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado de la carga. Las filas inválidas se descartan y se reportan en 'errors'.")
public record UploadResponse(
        @Schema(example = "inspecciones.csv") String fileName,
        @Schema(description = "Filas de datos leídas", example = "20") int totalRows,
        @Schema(description = "Filas cargadas", example = "18") int validRows,
        @Schema(description = "Filas descartadas", example = "2") int invalidRows,
        List<RowErrorResponse> errors) {

    static UploadResponse from(IngestionResult result) {
        return new UploadResponse(result.fileName(), result.totalRows(), result.validRows(), result.invalidRows(),
                result.errors().stream().map(RowErrorResponse::from).toList());
    }

    @Schema(description = "Error de validación de una fila")
    public record RowErrorResponse(
            @Schema(description = "Fila en el archivo (el encabezado es la fila 1)", example = "5") int row,
            @Schema(example = "Estado") String column,
            @Schema(example = "Estado no reconocido 'Aprobada'") String message) {

        static RowErrorResponse from(RowError e) {
            return new RowErrorResponse(e.row(), e.column(), e.message());
        }
    }
}
