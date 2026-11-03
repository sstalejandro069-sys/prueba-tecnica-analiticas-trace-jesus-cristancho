package co.analiticas.trace.ingestion.infrastructure.out;

import co.analiticas.trace.ingestion.application.port.out.FileParserPort;
import co.analiticas.trace.ingestion.domain.InvalidFileException;
import co.analiticas.trace.ingestion.domain.RawTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class CsvFileParser implements FileParserPort {

    private static final char BOM = '﻿';

    @Override
    public boolean supports(String extension) {
        return "csv".equals(extension);
    }

    @Override
    public RawTable parse(byte[] content) {
        var text = new String(content, StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == BOM) {
            text = text.substring(1);
        }
        if (text.isBlank()) {
            throw new InvalidFileException("El archivo CSV está vacío.");
        }

        var format = CSVFormat.DEFAULT.builder()
                .setDelimiter(detectDelimiter(text))
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setAllowMissingColumnNames(true)
                .get();

        try (CSVParser parser = CSVParser.parse(new StringReader(text), format)) {
            var headers = parser.getHeaderNames().stream().map(String::trim).toList();
            var rows = new ArrayList<RawTable.RawRow>();
            for (CSVRecord record : parser) {
                var values = new LinkedHashMap<String, String>();
                for (int i = 0; i < headers.size() && i < record.size(); i++) {
                    values.put(headers.get(i), record.get(i));
                }
                if (values.values().stream().allMatch(v -> v == null || v.isBlank())) {
                    continue;
                }
                // El encabezado ocupa la línea 1.
                rows.add(new RawTable.RawRow((int) record.getRecordNumber() + 1, values));
            }
            return new RawTable(headers, List.copyOf(rows));
        } catch (IOException | UncheckedIOException | IllegalArgumentException | IllegalStateException e) {
            throw new InvalidFileException("No se pudo leer el archivo CSV: " + e.getMessage());
        }
    }

    /** Excel en español exporta CSV con ';'. Se elige el separador más frecuente en la primera línea. */
    private static char detectDelimiter(String text) {
        var firstLine = text.lines().findFirst().orElse("");
        long commas = firstLine.chars().filter(c -> c == ',').count();
        long semicolons = firstLine.chars().filter(c -> c == ';').count();
        return semicolons > commas ? ';' : ',';
    }
}
