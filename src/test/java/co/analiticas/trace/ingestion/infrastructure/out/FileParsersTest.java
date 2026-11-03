package co.analiticas.trace.ingestion.infrastructure.out;

import co.analiticas.trace.ingestion.domain.InvalidFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileParsersTest {

    private final CsvFileParser csv = new CsvFileParser();
    private final XlsxFileParser xlsx = new XlsxFileParser();

    @Test
    void csvParsesCommaSeparatedWithQuotedValues() {
        var content = "ID,Fecha,Ubicacion,Estado\nA1,2026-10-01,\"Frente Norte, sector 2\",Aprobado\n\n";

        var table = csv.parse(content.getBytes(StandardCharsets.UTF_8));

        assertThat(table.headers()).containsExactly("ID", "Fecha", "Ubicacion", "Estado");
        assertThat(table.rows()).singleElement().satisfies(r -> {
            assertThat(r.rowNumber()).isEqualTo(2);
            assertThat(r.values()).containsEntry("Ubicacion", "Frente Norte, sector 2");
        });
    }

    @Test
    void csvDetectsSemicolonAndStripsBom() {
        var content = "﻿ID;Fecha;Ubicación;Estado\nA1;01/10/2026;Pozo 7;Pendiente\n";

        var table = csv.parse(content.getBytes(StandardCharsets.UTF_8));

        assertThat(table.headers()).first().isEqualTo("ID");
        assertThat(table.rows().getFirst().values()).containsEntry("Ubicación", "Pozo 7");
    }

    @Test
    void csvRejectsBlankFile() {
        assertThatThrownBy(() -> csv.parse("  \n".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void xlsxReadsFirstSheetAndFormatsDates() throws Exception {
        byte[] content;
        try (var workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet();
            var header = sheet.createRow(0);
            String[] names = {"ID", "Fecha", "Ubicacion", "Estado"};
            for (int i = 0; i < names.length; i++) {
                header.createCell(i).setCellValue(names[i]);
            }
            var dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue(101);
            var dateCell = row.createCell(1);
            dateCell.setCellValue(LocalDate.of(2026, 10, 5));
            dateCell.setCellStyle(dateStyle);
            row.createCell(2).setCellValue("Frente Sur");
            row.createCell(3).setCellValue("Rechazado");
            workbook.write(out);
            content = out.toByteArray();
        }

        var table = xlsx.parse(content);

        assertThat(table.rows()).singleElement().satisfies(r -> {
            assertThat(r.rowNumber()).isEqualTo(2);
            assertThat(r.values()).containsEntry("ID", "101").containsEntry("Fecha", "2026-10-05");
        });
    }

    @Test
    void xlsxRejectsCorruptFile() {
        assertThatThrownBy(() -> xlsx.parse("no soy un excel".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining(".xlsx");
    }
}
