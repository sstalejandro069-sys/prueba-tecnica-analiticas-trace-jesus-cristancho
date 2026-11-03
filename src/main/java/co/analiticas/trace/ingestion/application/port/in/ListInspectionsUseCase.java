package co.analiticas.trace.ingestion.application.port.in;

import co.analiticas.trace.ingestion.domain.InspectionRecord;

import java.util.List;

public interface ListInspectionsUseCase {

    List<InspectionRecord> listAll();
}
