package co.analiticas.trace.ingestion.domain;

import java.time.LocalDate;

public record InspectionRecord(
        String id,
        LocalDate date,
        String location,
        InspectionStatus status,
        String inspector,
        String observations) {
}
