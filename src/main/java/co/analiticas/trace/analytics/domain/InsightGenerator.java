package co.analiticas.trace.analytics.domain;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reglas que convierten las métricas en hallazgos legibles en español. */
final class InsightGenerator {

    static final String REJECTED = "Rechazado";
    static final String PENDING = "Pendiente";
    static final double REJECTION_ALERT_THRESHOLD = 30.0;

    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int total;
    private final List<CategoryCount> byStatus;
    private final List<CategoryCount> byLocation;
    private final List<CategoryCount> rejectedByLocation;
    private final LocalDate first;
    private final LocalDate last;

    InsightGenerator(int total, List<CategoryCount> byStatus, List<CategoryCount> byLocation,
                     List<CategoryCount> rejectedByLocation, LocalDate first, LocalDate last) {
        this.total = total;
        this.byStatus = byStatus;
        this.byLocation = byLocation;
        this.rejectedByLocation = rejectedByLocation;
        this.first = first;
        this.last = last;
    }

    String summary() {
        if (total == 0) {
            return "Aún no se ha cargado ningún archivo de inspecciones.";
        }
        var topStatus = leaders(byStatus);
        var topLocation = leaders(byLocation);
        return "Se procesaron %d %s: el %s se encuentra en estado %s, siendo %s %s con mayor actividad."
                .formatted(total, plural(total, "registro", "registros"),
                        percent(topStatus.getFirst().percentage()), joinLabels(topStatus),
                        joinLabels(topLocation), topLocation.size() > 1 ? "las zonas" : "la zona");
    }

    List<String> insights() {
        if (total == 0) {
            return List.of();
        }
        var insights = new ArrayList<String>();

        insights.add("Distribución por estado: " + String.join(", ", byStatus.stream()
                .map(c -> "%s %s (%d)".formatted(c.label(), percent(c.percentage()), c.count()))
                .toList()) + ".");

        var topLocation = leaders(byLocation);
        if (topLocation.size() == 1) {
            var top = topLocation.getFirst();
            insights.add("%s concentra el %s de las inspecciones (%d de %d) entre %d %s."
                    .formatted(top.label(), percent(top.percentage()), top.count(), total,
                            byLocation.size(), plural(byLocation.size(), "zona", "zonas")));
        } else {
            insights.add("Hay empate en mayor actividad entre %s, con %d inspecciones cada una."
                    .formatted(joinLabels(topLocation), topLocation.getFirst().count()));
        }

        find(REJECTED).ifPresentOrElse(rejected -> {
            if (rejected.percentage() >= REJECTION_ALERT_THRESHOLD) {
                insights.add("Alerta: la tasa de rechazo (%s) supera el umbral del %s. Se recomienda revisar los frentes afectados."
                        .formatted(percent(rejected.percentage()), percent(REJECTION_ALERT_THRESHOLD)));
            }
            var worst = leaders(rejectedByLocation);
            insights.add("%s con más rechazos: %s (%d)."
                    .formatted(worst.size() > 1 ? "Zonas" : "Zona", joinLabels(worst), worst.getFirst().count()));
        }, () -> insights.add("No se registran inspecciones rechazadas."));

        find(PENDING).ifPresent(pending -> insights.add("%d %s pendiente%s de cierre (%s)."
                .formatted(pending.count(), plural(pending.count(), "inspección", "inspecciones"),
                        pending.count() == 1 ? "" : "s", percent(pending.percentage()))));

        if (first != null) {
            insights.add(first.equals(last)
                    ? "Periodo analizado: " + DATE.format(first) + "."
                    : "Periodo analizado: del %s al %s.".formatted(DATE.format(first), DATE.format(last)));
        }
        return List.copyOf(insights);
    }

    private java.util.Optional<CategoryCount> find(String label) {
        return byStatus.stream().filter(c -> c.label().equalsIgnoreCase(label)).findFirst();
    }

    /** Categorías empatadas en el primer lugar (la lista ya viene ordenada). */
    private static List<CategoryCount> leaders(List<CategoryCount> counts) {
        var max = counts.getFirst().count();
        return counts.stream().filter(c -> c.count() == max).toList();
    }

    private static String joinLabels(List<CategoryCount> counts) {
        var labels = counts.stream().map(CategoryCount::label).toList();
        if (labels.size() == 1) {
            return labels.getFirst();
        }
        return String.join(", ", labels.subList(0, labels.size() - 1)) + " y " + labels.getLast();
    }

    private static String percent(double value) {
        return new DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(ES_CO)).format(value) + "%";
    }

    private static String plural(long count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }
}
