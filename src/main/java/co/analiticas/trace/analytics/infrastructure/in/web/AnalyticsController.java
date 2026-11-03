package co.analiticas.trace.analytics.infrastructure.in.web;

import co.analiticas.trace.analytics.application.port.in.GetAnalyticsUseCase;
import co.analiticas.trace.shared.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analítica", description = "Métricas descriptivas y resumen automático")
public class AnalyticsController {

    private final GetAnalyticsUseCase getAnalytics;

    public AnalyticsController(GetAnalyticsUseCase getAnalytics) {
        this.getAnalytics = getAnalytics;
    }

    @GetMapping
    @Operation(summary = "Obtener analítica",
            description = "Distribución por estado y ubicación, periodo analizado, resumen y hallazgos automáticos.")
    @ApiResponse(responseCode = "200", description = "Reporte (totalRecords = 0 si no hay datos cargados)")
    @ApiResponse(responseCode = "401", description = "Token ausente o inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public AnalyticsResponse get() {
        return AnalyticsResponse.from(getAnalytics.generate());
    }
}
