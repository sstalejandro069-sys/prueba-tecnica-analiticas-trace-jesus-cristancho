package co.analiticas.trace.analytics.domain;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Calcula las métricas descriptivas y delega la redacción de hallazgos al {@link InsightGenerator}. */
public final class AnalyticsCalculator {

    private AnalyticsCalculator() {
    }

    public static AnalyticsReport analyze(List<InspectionFact> facts) {
        int total = facts.size();
        var byStatus = countBy(facts, InspectionFact::status, total);
        var byLocation = countBy(facts, InspectionFact::location, total);
        var dates = facts.stream().map(InspectionFact::date).filter(Objects::nonNull).toList();
        LocalDate first = dates.stream().min(Comparator.naturalOrder()).orElse(null);
        LocalDate last = dates.stream().max(Comparator.naturalOrder()).orElse(null);
        var rejectedByLocation = countBy(
                facts.stream().filter(f -> InsightGenerator.REJECTED.equalsIgnoreCase(f.status())).toList(),
                InspectionFact::location, total);

        var insights = new InsightGenerator(total, byStatus, byLocation, rejectedByLocation, first, last);
        return new AnalyticsReport(total, byStatus, byLocation, first, last, insights.summary(), insights.insights());
    }

    /** Agrupa y ordena de mayor a menor frecuencia; en empate, alfabéticamente. */
    private static List<CategoryCount> countBy(List<InspectionFact> facts, Function<InspectionFact, String> key, int total) {
        Map<String, Long> counts = facts.stream().collect(Collectors.groupingBy(key, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue(), total == 0 ? 0 : round(e.getValue() * 100.0 / total)))
                .sorted(Comparator.comparingLong(CategoryCount::count).reversed().thenComparing(CategoryCount::label))
                .toList();
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
