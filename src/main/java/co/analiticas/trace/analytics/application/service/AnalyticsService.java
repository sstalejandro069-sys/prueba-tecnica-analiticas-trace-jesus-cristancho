package co.analiticas.trace.analytics.application.service;

import co.analiticas.trace.analytics.application.port.in.GetAnalyticsUseCase;
import co.analiticas.trace.analytics.application.port.out.InspectionQueryPort;
import co.analiticas.trace.analytics.domain.AnalyticsCalculator;
import co.analiticas.trace.analytics.domain.AnalyticsReport;

public class AnalyticsService implements GetAnalyticsUseCase {

    private final InspectionQueryPort inspections;

    public AnalyticsService(InspectionQueryPort inspections) {
        this.inspections = inspections;
    }

    @Override
    public AnalyticsReport generate() {
        return AnalyticsCalculator.analyze(inspections.findAll());
    }
}
