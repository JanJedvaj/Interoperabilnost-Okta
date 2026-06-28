package hr.algebra.iis.okta.application.controller;

import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.importing.ApplicationImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/applications/import")
public class ApplicationImportController {

    private final ApplicationImportService applicationImportService;

    public ApplicationImportController(ApplicationImportService applicationImportService) {
        this.applicationImportService = applicationImportService;
    }

    @PostMapping(path = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationResponse> importJson(@RequestBody String document) {
        ApplicationResponse response = applicationImportService.importJson(document);
        return created(response);
    }

    @PostMapping(path = "/xml", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<ApplicationResponse> importXml(@RequestBody String document) {
        ApplicationResponse response = applicationImportService.importXml(document);
        return created(response);
    }

    private static ResponseEntity<ApplicationResponse> created(ApplicationResponse response) {
        return ResponseEntity
                .created(URI.create("/api/applications/" + response.id()))
                .body(response);
    }
}
