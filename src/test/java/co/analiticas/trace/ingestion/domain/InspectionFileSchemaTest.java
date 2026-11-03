package co.analiticas.trace.ingestion.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspectionFileSchemaTest {

    private static final List<String> HEADERS = List.of("ID", "Fecha", "Ubicación", "Estado", "Inspector");

    @Test
    void acceptsValidRowsWithAccentedAndCaseInsensitiveHeaders() {
        var table = new RawTable(List.of("id", "FECHA", "Ubicación", "estado"), List.of(
                row(2, Map.of("id", "A1", "FECHA", "2026-10-01", "Ubicación", "Frente Norte", "estado", "aprobado"))));

        var result = InspectionFileSchema.validate("f.csv", table);

        assertThat(result.records()).singleElement().satisfies(r -> {
            assertThat(r.status()).isEqualTo(InspectionStatus.APROBADO);
            assertThat(r.date()).isEqualTo(LocalDate.of(2026, 10, 1));
            assertThat(r.location()).isEqualTo("Frente Norte");
        });
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void rejectsFileWithMissingRequiredColumns() {
        var table = new RawTable(List.of("Codigo", "Fecha"), List.of());

        assertThatThrownBy(() -> InspectionFileSchema.validate("f.csv", table))
                .isInstanceOf(InvalidFileContentException.class)
                .satisfies(e -> assertThat(((InvalidFileContentException) e).getDetails().getFirst())
                        .contains("ID", "Ubicacion", "Estado"));
    }

    @Test
    void reportsRowErrorsAndKeepsValidRows() {
        var table = new RawTable(HEADERS, List.of(
                row(2, values("A1", "2026-10-01", "Norte", "Aprobado")),
                row(3, values("A2", "31/02/2026", "Sur", "Aprobado")),
                row(4, values("A3", "2026-10-02", "", "Rechazado")),
                row(5, values("A4", "2026-10-02", "Sur", "Aprobada")),
                row(6, values("A1", "2026-10-03", "Norte", "Pendiente")),
                row(7, values("A5", "05/10/2026", "Pozo 7", "Rechazado"))));

        var result = InspectionFileSchema.validate("f.csv", table);

        assertThat(result.totalRows()).isEqualTo(6);
        assertThat(result.records()).extracting(InspectionRecord::id).containsExactly("A1", "A5");
        assertThat(result.errors()).extracting(RowError::row, RowError::column).containsExactly(
                org.assertj.core.groups.Tuple.tuple(3, "Fecha"),
                org.assertj.core.groups.Tuple.tuple(4, "Ubicacion"),
                org.assertj.core.groups.Tuple.tuple(5, "Estado"),
                org.assertj.core.groups.Tuple.tuple(6, "ID"));
    }

    @Test
    void failsWhenNoRowIsValid() {
        var table = new RawTable(HEADERS, List.of(row(2, values("A1", "ayer", "Norte", "Aprobado"))));

        assertThatThrownBy(() -> InspectionFileSchema.validate("f.csv", table))
                .isInstanceOf(InvalidFileContentException.class)
                .hasMessageContaining("Ningún registro");
    }

    @Test
    void failsWhenFileHasNoDataRows() {
        var table = new RawTable(HEADERS, List.of());

        assertThatThrownBy(() -> InspectionFileSchema.validate("f.csv", table))
                .isInstanceOf(InvalidFileContentException.class)
                .hasMessageContaining("no contiene registros");
    }

    private static Map<String, String> values(String id, String date, String location, String status) {
        var map = new LinkedHashMap<String, String>();
        map.put("ID", id);
        map.put("Fecha", date);
        map.put("Ubicación", location);
        map.put("Estado", status);
        return map;
    }

    private static RawTable.RawRow row(int number, Map<String, String> values) {
        return new RawTable.RawRow(number, values);
    }
}
