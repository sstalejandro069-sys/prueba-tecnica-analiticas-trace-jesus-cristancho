package co.analiticas.trace.ingestion.application.port.out;

import co.analiticas.trace.ingestion.domain.InspectionRecord;

import java.util.List;

public interface InspectionRepositoryPort {

    /** Cada carga reemplaza el conjunto de datos anterior. */
    void replaceAll(List<InspectionRecord> records);

    List<InspectionRecord> findAll();
}
