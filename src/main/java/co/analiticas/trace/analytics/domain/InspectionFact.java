package co.analiticas.trace.analytics.domain;

import java.time.LocalDate;

/**
 * Vista mínima de una inspección que necesita la analítica. Es propia de este
 * módulo para no acoplarse al modelo de dominio de la ingesta.
 */
public record InspectionFact(String status, String location, LocalDate date) {
}
