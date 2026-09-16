package in.apnabazaar.ingestion;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
public class IngestionController {

    private final ExcelIngestionService excelIngestionService;

    public IngestionController(ExcelIngestionService excelIngestionService) {
        this.excelIngestionService = excelIngestionService;
    }

    @PostMapping("/admin/upload")
    public IngestionResult upload(@RequestParam String communitySlug, @RequestPart MultipartFile file) {
        try {
            return excelIngestionService.ingest(file, communitySlug);
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Could not read the uploaded file: " + e.getMessage());
        }
    }
}
