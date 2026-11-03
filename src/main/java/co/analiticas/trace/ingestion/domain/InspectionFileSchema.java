package co.analiticas.trace.ingestion.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Esquema esperado del reporte de inspecciones y reglas para convertir
 * cada fila cruda en un {@link InspectionRecord} válido.
 */
public final class InspectionFileSchema {

    public static final String ID = "ID";
    public static final String DATE = "Fecha";
    public static final String LOCATION = "Ubicacion";
    public static final String STATUS = "Estado";
    public static final String INSPECTOR = "Inspector";
    public static final String OBSERVATIONS = "Observaciones";

    public static final List<String> REQUIRED_COLUMNS = List.of(ID, DATE, LOCATION, STATUS);
    public static final List<String> OPTIONAL_COLUMNS = List.of(INSPECTOR, OBSERVATIONS);

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT));

    private InspectionFileSchema() {
    }

    public static IngestionResult validate(String fileName, RawTable table) {
        var columns = resolveColumns(table.headers());
        var missing = REQUIRED_COLUMNS.stream().filter(c -> !columns.containsKey(c)).toList();
        if (!missing.isEmpty()) {
            throw new InvalidFileContentException(
                    "El archivo no cumple con el esquema esperado: faltan columnas obligatorias.",
                    List.of("Columnas faltantes: " + String.join(", ", missing),
                            "Columnas esperadas: " + String.join(", ", REQUIRED_COLUMNS)
                                    + " (opcionales: " + String.join(", ", OPTIONAL_COLUMNS) + ")"));
        }
        if (table.rows().isEmpty()) {
            throw new InvalidFileContentException("El archivo no contiene registros de datos.", List.of());
        }

        var records = new ArrayList<InspectionRecord>();
        var errors = new ArrayList<RowError>();
        Set<String> seenIds = new HashSet<>();

        for (var row : table.rows()) {
            var rowErrors = new ArrayList<RowError>();
            var values = new RowValues(row, columns);

            var id = values.required(ID, rowErrors);
            if (id != null && !seenIds.add(id)) {
                rowErrors.add(new RowError(row.rowNumber(), ID, "ID duplicado: " + id));
            }
            var date = parseDate(values.required(DATE, rowErrors), row.rowNumber(), rowErrors);
            var location = values.required(LOCATION, rowErrors);
            var status = parseStatus(values.required(STATUS, rowErrors), row.rowNumber(), rowErrors);

            if (rowErrors.isEmpty()) {
                records.add(new InspectionRecord(id, date, location, status,
                        values.optional(INSPECTOR), values.optional(OBSERVATIONS)));
            } else {
                errors.addAll(rowErrors);
            }
        }

        if (records.isEmpty()) {
            throw new InvalidFileContentException("Ningún registro del archivo es válido.",
                    errors.stream().map(InspectionFileSchema::describe).toList());
        }
        return new IngestionResult(fileName, table.rows().size(), List.copyOf(records), List.copyOf(errors));
    }

    /** Asocia cada columna del esquema con el encabezado real del archivo, sin importar tildes ni mayúsculas. */
    private static Map<String, String> resolveColumns(List<String> headers) {
        Map<String, String> byNormalized = new HashMap<>();
        headers.forEach(h -> byNormalized.putIfAbsent(TextNormalizer.normalize(h), h));

        Map<String, String> resolved = new HashMap<>();
        for (String column : concat(REQUIRED_COLUMNS, OPTIONAL_COLUMNS)) {
            var actual = byNormalized.get(TextNormalizer.normalize(column));
            if (actual != null) {
                resolved.put(column, actual);
            }
        }
        return resolved;
    }

    private static LocalDate parseDate(String value, int row, List<RowError> errors) {
        if (value == null) {
            return null;
        }
        for (var format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException ignored) {
                // se intenta el siguiente formato
            }
        }
        errors.add(new RowError(row, DATE, "Fecha inválida '" + value + "' (use AAAA-MM-DD o DD/MM/AAAA)"));
        return null;
    }

    private static InspectionStatus parseStatus(String value, int row, List<RowError> errors) {
        if (value == null) {
            return null;
        }
        return InspectionStatus.fromLabel(value).orElseGet(() -> {
            errors.add(new RowError(row, STATUS,
                    "Estado no reconocido '" + value + "' (valores permitidos: " + InspectionStatus.allowedLabels() + ")"));
            return null;
        });
    }

    private static String describe(RowError error) {
        return "Fila " + error.row() + " - " + error.column() + ": " + error.message();
    }

    private static List<String> concat(List<String> a, List<String> b) {
        var all = new ArrayList<>(a);
        all.addAll(b);
        return all;
    }

    private record RowValues(RawTable.RawRow row, Map<String, String> columns) {

        String required(String column, List<RowError> errors) {
            var value = optional(column);
            if (value == null) {
                errors.add(new RowError(row.rowNumber(), column, "Campo obligatorio vacío"));
            }
            return value;
        }

        String optional(String column) {
            var header = columns.get(column);
            if (header == null) {
                return null;
            }
            var value = row.values().get(header);
            return value == null || value.isBlank() ? null : value.trim();
        }
    }
}
