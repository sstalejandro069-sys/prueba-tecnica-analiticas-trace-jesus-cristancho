package co.analiticas.trace.analytics.infrastructure.config;

import co.analiticas.trace.analytics.application.port.out.InspectionQueryPort;
import co.analiticas.trace.analytics.application.service.AnalyticsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalyticsConfig {

    @Bean
    AnalyticsService analyticsService(InspectionQueryPort inspections) {
        return new AnalyticsService(inspections);
    }
}
