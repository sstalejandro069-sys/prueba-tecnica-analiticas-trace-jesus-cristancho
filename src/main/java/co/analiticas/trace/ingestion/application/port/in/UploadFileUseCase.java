package co.analiticas.trace.ingestion.application.port.in;

import co.analiticas.trace.ingestion.domain.IngestionResult;

public interface UploadFileUseCase {

    IngestionResult upload(UploadFileCommand command);

    record UploadFileCommand(String fileName, byte[] content) {
    }
}
