package co.analiticas.trace.ingestion.application.service;

import co.analiticas.trace.ingestion.application.port.in.ListInspectionsUseCase;
import co.analiticas.trace.ingestion.application.port.in.UploadFileUseCase;
import co.analiticas.trace.ingestion.application.port.out.FileParserPort;
import co.analiticas.trace.ingestion.application.port.out.InspectionRepositoryPort;
import co.analiticas.trace.ingestion.domain.IngestionResult;
import co.analiticas.trace.ingestion.domain.InspectionFileSchema;
import co.analiticas.trace.ingestion.domain.InspectionRecord;
import co.analiticas.trace.ingestion.domain.InvalidFileException;
import co.analiticas.trace.ingestion.domain.UnsupportedFileTypeException;

import java.util.List;
import java.util.Locale;

public class IngestionService implements UploadFileUseCase, ListInspectionsUseCase {

    private final List<FileParserPort> parsers;
    private final InspectionRepositoryPort repository;

    public IngestionService(List<FileParserPort> parsers, InspectionRepositoryPort repository) {
        this.parsers = List.copyOf(parsers);
        this.repository = repository;
    }

    @Override
    public IngestionResult upload(UploadFileCommand command) {
        var fileName = command.fileName() == null ? "" : command.fileName();
        if (command.content() == null || command.content().length == 0) {
            throw new InvalidFileException("El archivo está vacío.");
        }

        var extension = extensionOf(fileName);
        var parser = parsers.stream()
                .filter(p -> p.supports(extension))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileTypeException(fileName));

        var result = InspectionFileSchema.validate(fileName, parser.parse(command.content()));
        repository.replaceAll(result.records());
        return result;
    }

    @Override
    public List<InspectionRecord> listAll() {
        return repository.findAll();
    }

    private static String extensionOf(String fileName) {
        var dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
