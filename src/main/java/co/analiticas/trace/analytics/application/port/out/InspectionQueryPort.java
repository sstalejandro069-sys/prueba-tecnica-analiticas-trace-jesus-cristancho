package co.analiticas.trace.analytics.application.port.out;

import co.analiticas.trace.analytics.domain.InspectionFact;

import java.util.List;

/** Fuente de datos de la analítica. Hoy la provee el módulo de ingesta; mañana podría ser un data warehouse. */
public interface InspectionQueryPort {

    List<InspectionFact> findAll();
}
