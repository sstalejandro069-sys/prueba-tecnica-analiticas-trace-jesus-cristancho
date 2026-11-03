package co.analiticas.trace.analytics.domain;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsReport(
        int totalRecords,
        List<CategoryCount> byStatus,
        List<CategoryCount> byLocation,
        LocalDate firstDate,
        LocalDate lastDate,
        String summary,
        List<String> insights) {
}
