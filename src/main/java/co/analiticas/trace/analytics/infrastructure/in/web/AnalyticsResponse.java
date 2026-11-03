package co.analiticas.trace.analytics.infrastructure.in.web;

import co.analiticas.trace.analytics.domain.AnalyticsReport;
import co.analiticas.trace.analytics.domain.CategoryCount;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Métricas descriptivas y hallazgos automáticos de la última carga")
public record AnalyticsResponse(
        @Schema(example = "4") int totalRecords,
        @Schema(description = "Distribución por estado, de mayor a menor") List<CategoryResponse> byStatus,
        @Schema(description = "Distribución por ubicación, de mayor a menor") List<CategoryResponse> byLocation,
        @Schema(example = "2026-10-01") LocalDate firstDate,
        @Schema(example = "2026-10-15") LocalDate lastDate,
        @Schema(example = "Se procesaron 4 registros: el 50% se encuentra en estado Aprobado, siendo Frente Norte la zona con mayor actividad.")
        String summary,
        @Schema(description = "Hallazgos adicionales generados por reglas") List<String> insights) {

    static AnalyticsResponse from(AnalyticsReport r) {
        return new AnalyticsResponse(r.totalRecords(), map(r.byStatus()), map(r.byLocation()),
                r.firstDate(), r.lastDate(), r.summary(), r.insights());
    }

    private static List<CategoryResponse> map(List<CategoryCount> counts) {
        return counts.stream().map(c -> new CategoryResponse(c.label(), c.count(), c.percentage())).toList();
    }

    @Schema(description = "Conteo de una categoría")
    public record CategoryResponse(
            @Schema(example = "Aprobado") String label,
            @Schema(example = "2") long count,
            @Schema(description = "Porcentaje sobre el total (1 decimal)", example = "50.0") double percentage) {
    }
}
