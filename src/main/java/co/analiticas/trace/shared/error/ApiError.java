package co.analiticas.trace.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Formato común de error de la API")
public record ApiError(
        @Schema(example = "422") int status,
        @Schema(description = "Código estable para el cliente", example = "INVALID_FILE_CONTENT") String code,
        @Schema(example = "El archivo no cumple con el esquema esperado: faltan columnas obligatorias.") String message,
        @Schema(description = "Detalle adicional (columnas faltantes, errores por fila, campos inválidos)",
                example = "[\"Columnas faltantes: Estado\"]") List<String> details,
        @Schema(example = "/api/inspections/upload") String path,
        Instant timestamp) {
}
