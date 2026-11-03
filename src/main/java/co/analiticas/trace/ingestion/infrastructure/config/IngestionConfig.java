package co.analiticas.trace.ingestion.infrastructure.config;

import co.analiticas.trace.ingestion.application.port.out.FileParserPort;
import co.analiticas.trace.ingestion.application.port.out.InspectionRepositoryPort;
import co.analiticas.trace.ingestion.application.service.IngestionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class IngestionConfig {

    /** Spring inyecta todos los adaptadores FileParserPort: agregar un formato no toca el servicio. */
    @Bean
    IngestionService ingestionService(List<FileParserPort> parsers, InspectionRepositoryPort repository) {
        return new IngestionService(parsers, repository);
    }
}
