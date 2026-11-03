package co.analiticas.trace.ingestion.domain;

import java.text.Normalizer;
import java.util.Locale;

/** Normaliza encabezados y valores: sin tildes, minúsculas y sin espacios sobrantes. */
final class TextNormalizer {

    private TextNormalizer() {
    }

    static String normalize(String value) {
        var withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).replaceAll("[\\s_]+", " ");
    }
}
