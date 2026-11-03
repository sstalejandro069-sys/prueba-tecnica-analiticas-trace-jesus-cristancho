package co.analiticas.trace.ingestion.infrastructure.out;

import co.analiticas.trace.ingestion.application.port.out.InspectionRepositoryPort;
import co.analiticas.trace.ingestion.domain.InspectionRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** Persistencia en memoria para el PoC. Un adaptador JPA implementaría el mismo puerto. */
@Component
public class InMemoryInspectionRepository implements InspectionRepositoryPort {

    private final AtomicReference<List<InspectionRecord>> records = new AtomicReference<>(List.of());

    @Override
    public void replaceAll(List<InspectionRecord> newRecords) {
        records.set(List.copyOf(newRecords));
    }

    @Override
    public List<InspectionRecord> findAll() {
        return records.get();
    }
}
