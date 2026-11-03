package co.analiticas.trace.ingestion.infrastructure.in.web;

import co.analiticas.trace.ingestion.domain.InspectionRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Registro de inspección válido")
public record InspectionResponse(
        @Schema(example = "INS-001") String id,
        @Schema(example = "2026-10-01") LocalDate date,
        @Schema(example = "Frente Norte") String location,
        @Schema(example = "Aprobado", allowableValues = {"Aprobado", "Rechazado", "Pendiente"}) String status,
        @Schema(example = "Carlos Rojas") String inspector,
        @Schema(example = "Sin novedades") String observations) {

    static InspectionResponse from(InspectionRecord r) {
        return new InspectionResponse(r.id(), r.date(), r.location(), r.status().label(), r.inspector(), r.observations());
    }
}
