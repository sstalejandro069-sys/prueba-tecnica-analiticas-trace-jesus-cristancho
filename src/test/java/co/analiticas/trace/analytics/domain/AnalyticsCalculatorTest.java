package co.analiticas.trace.analytics.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsCalculatorTest {

    @Test
    void producesSummaryLikeTheSpecificationExample() {
        var report = AnalyticsCalculator.analyze(List.of(
                fact("Aprobado", "Frente Norte", "2026-10-01"),
                fact("Aprobado", "Frente Norte", "2026-10-02"),
                fact("Rechazado", "Frente Sur", "2026-10-03"),
                fact("Pendiente", "Planta", "2026-10-04")));

        assertThat(report.totalRecords()).isEqualTo(4);
        assertThat(report.summary()).isEqualTo(
                "Se procesaron 4 registros: el 50% se encuentra en estado Aprobado, siendo Frente Norte la zona con mayor actividad.");
        assertThat(report.byStatus()).extracting(CategoryCount::label).containsExactly("Aprobado", "Pendiente", "Rechazado");
        assertThat(report.byLocation().getFirst()).isEqualTo(new CategoryCount("Frente Norte", 2, 50.0));
        assertThat(report.firstDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(report.insights()).anyMatch(i -> i.startsWith("Periodo analizado: del 01/10/2026 al 04/10/2026"));
        assertThat(report.insights()).noneMatch(i -> i.startsWith("Alerta"));
    }

    @Test
    void raisesAlertWhenRejectionRateReachesThreshold() {
        var report = AnalyticsCalculator.analyze(List.of(
                fact("Rechazado", "Pozo 7", "2026-10-01"),
                fact("Aprobado", "Pozo 7", "2026-10-01"),
                fact("Aprobado", "Frente Sur", "2026-10-01")));

        assertThat(report.insights())
                .anyMatch(i -> i.startsWith("Alerta: la tasa de rechazo (33,3%)"))
                .contains("Zona con más rechazos: Pozo 7 (1).")
                .contains("Periodo analizado: 01/10/2026.");
    }

    @Test
    void describesTiesBetweenLeaders() {
        var report = AnalyticsCalculator.analyze(List.of(
                fact("Aprobado", "Norte", "2026-10-01"),
                fact("Pendiente", "Sur", "2026-10-02")));

        assertThat(report.summary()).contains("Aprobado y Pendiente", "Norte y Sur las zonas");
        assertThat(report.insights()).contains("No se registran inspecciones rechazadas.");
    }

    @Test
    void emptyDataProducesFriendlyMessage() {
        var report = AnalyticsCalculator.analyze(List.of());

        assertThat(report.totalRecords()).isZero();
        assertThat(report.summary()).contains("no se ha cargado");
        assertThat(report.insights()).isEmpty();
    }

    private static InspectionFact fact(String status, String location, String date) {
        return new InspectionFact(status, location, LocalDate.parse(date));
    }
}
