package co.analiticas.trace.analytics.infrastructure.out;

import co.analiticas.trace.analytics.application.port.out.InspectionQueryPort;
import co.analiticas.trace.analytics.domain.InspectionFact;
import co.analiticas.trace.ingestion.application.port.in.ListInspectionsUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Único punto de contacto entre analítica e ingesta: consume el puerto de
 * entrada de la ingesta y traduce sus registros al modelo de la analítica.
 */
@Component
public class IngestionInspectionQueryAdapter implements InspectionQueryPort {

    private final ListInspectionsUseCase listInspections;

    public IngestionInspectionQueryAdapter(ListInspectionsUseCase listInspections) {
        this.listInspections = listInspections;
    }

    @Override
    public List<InspectionFact> findAll() {
        return listInspections.listAll().stream()
                .map(r -> new InspectionFact(r.status().label(), r.location(), r.date()))
                .toList();
    }
}
