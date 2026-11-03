package co.analiticas.trace.ingestion.domain;

import java.util.List;

public record IngestionResult(String fileName, int totalRows, List<InspectionRecord> records, List<RowError> errors) {

    public int validRows() {
        return records.size();
    }

    public int invalidRows() {
        return totalRows - records.size();
    }
}
