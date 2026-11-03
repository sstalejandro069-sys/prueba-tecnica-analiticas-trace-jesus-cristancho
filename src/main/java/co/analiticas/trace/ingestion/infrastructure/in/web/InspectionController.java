package co.analiticas.trace.ingestion.infrastructure.in.web;

import co.analiticas.trace.ingestion.application.port.in.ListInspectionsUseCase;
import co.analiticas.trace.ingestion.application.port.in.UploadFileUseCase;
import co.analiticas.trace.ingestion.application.port.in.UploadFileUseCase.UploadFileCommand;
import co.analiticas.trace.ingestion.domain.InvalidFileException;
import co.analiticas.trace.shared.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/inspections")
@Tag(name = "Ingesta", description = "Carga de reportes de inspección (.csv / .xlsx) y consulta de registros")
public class InspectionController {

    private final UploadFileUseCase uploadFile;
    private final ListInspectionsUseCase listInspections;

    public InspectionController(UploadFileUseCase uploadFile, ListInspectionsUseCase listInspections) {
        this.uploadFile = uploadFile;
        this.listInspections = listInspections;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cargar reporte de inspecciones",
            description = """
                    Columnas obligatorias: ID, Fecha, Ubicacion, Estado. Opcionales: Inspector, Observaciones.
                    Fecha en formato AAAA-MM-DD o DD/MM/AAAA. Estado: Aprobado, Rechazado o Pendiente.
                    Las filas válidas reemplazan la carga anterior; las inválidas se reportan en 'errors'.""")
    @ApiResponse(responseCode = "200", description = "Archivo procesado (puede incluir filas descartadas)")
    @ApiResponse(responseCode = "400", description = "Archivo vacío, dañado o de tipo no soportado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente o inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "413", description = "El archivo supera los 5 MB",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "422", description = "Faltan columnas obligatorias o ninguna fila es válida",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public UploadResponse upload(
            @Parameter(description = "Archivo .csv o .xlsx", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            var result = uploadFile.upload(new UploadFileCommand(file.getOriginalFilename(), file.getBytes()));
            return UploadResponse.from(result);
        } catch (IOException e) {
            throw new InvalidFileException("No se pudo leer el archivo enviado.");
        }
    }

    @GetMapping
    @Operation(summary = "Listar inspecciones", description = "Registros válidos de la última carga.")
    @ApiResponse(responseCode = "200", description = "Lista de registros (vacía si no se ha cargado archivo)")
    @ApiResponse(responseCode = "401", description = "Token ausente o inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<InspectionResponse> list() {
        return listInspections.listAll().stream().map(InspectionResponse::from).toList();
    }
}
