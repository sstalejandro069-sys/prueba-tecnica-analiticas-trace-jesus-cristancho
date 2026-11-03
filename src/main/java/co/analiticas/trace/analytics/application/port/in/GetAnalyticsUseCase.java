package co.analiticas.trace.analytics.application.port.in;

import co.analiticas.trace.analytics.domain.AnalyticsReport;

public interface GetAnalyticsUseCase {

    AnalyticsReport generate();
}
