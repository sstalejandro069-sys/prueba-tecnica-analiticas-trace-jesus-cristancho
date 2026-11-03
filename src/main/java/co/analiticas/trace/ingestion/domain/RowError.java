package co.analiticas.trace.ingestion.domain;

public record RowError(int row, String column, String message) {
}
