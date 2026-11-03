package co.analiticas.trace.ingestion.infrastructure.out;

import co.analiticas.trace.ingestion.application.port.out.FileParserPort;
import co.analiticas.trace.ingestion.domain.InvalidFileException;
import co.analiticas.trace.ingestion.domain.RawTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class XlsxFileParser implements FileParserPort {

    private final DataFormatter formatter = new DataFormatter();

    @Override
    public boolean supports(String extension) {
        return "xlsx".equals(extension);
    }

    @Override
    public RawTable parse(byte[] content) {
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidFileException("El libro de Excel no tiene hojas.");
            }
            return readSheet(workbook.getSheetAt(0));
        } catch (InvalidFileException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileException("No se pudo leer el archivo Excel. Verifique que sea un .xlsx válido.");
        }
    }

    private RawTable readSheet(Sheet sheet) {
        Row headerRow = null;
        int index = sheet.getFirstRowNum();
        // Se toma como encabezado la primera fila con contenido.
        for (; index <= sheet.getLastRowNum() && index >= 0; index++) {
            var row = sheet.getRow(index);
            if (row != null && !isBlank(row)) {
                headerRow = row;
                break;
            }
        }
        if (headerRow == null) {
            throw new InvalidFileException("La primera hoja del Excel está vacía.");
        }

        var headers = new ArrayList<String>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            headers.add(text(headerRow.getCell(c)).trim());
        }

        var rows = new ArrayList<RawTable.RawRow>();
        for (int r = index + 1; r <= sheet.getLastRowNum(); r++) {
            var row = sheet.getRow(r);
            if (row == null || isBlank(row)) {
                continue;
            }
            var values = new LinkedHashMap<String, String>();
            for (int c = 0; c < headers.size(); c++) {
                values.put(headers.get(c), text(row.getCell(c)));
            }
            rows.add(new RawTable.RawRow(r + 1, values));
        }
        return new RawTable(List.copyOf(headers), List.copyOf(rows));
    }

    private String text(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        return formatter.formatCellValue(cell);
    }

    private boolean isBlank(Row row) {
        for (int c = row.getFirstCellNum(); c >= 0 && c < row.getLastCellNum(); c++) {
            if (!text(row.getCell(c)).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
