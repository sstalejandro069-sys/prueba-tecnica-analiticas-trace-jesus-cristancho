package co.analiticas.trace.ingestion.domain;

import java.util.Arrays;
import java.util.Optional;

public enum InspectionStatus {
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    PENDIENTE("Pendiente");

    private final String label;

    InspectionStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Optional<InspectionStatus> fromLabel(String value) {
        if (value == null) {
            return Optional.empty();
        }
        var normalized = TextNormalizer.normalize(value);
        return Arrays.stream(values())
                .filter(s -> TextNormalizer.normalize(s.label).equals(normalized))
                .findFirst();
    }

    public static String allowedLabels() {
        return String.join(", ", Arrays.stream(values()).map(InspectionStatus::label).toList());
    }
}
