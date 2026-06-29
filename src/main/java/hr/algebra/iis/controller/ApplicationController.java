package hr.algebra.iis.controller;

import hr.algebra.iis.model.Application;
import hr.algebra.iis.service.ApplicationService;
import hr.algebra.iis.service.OktaService;
import hr.algebra.iis.service.XmlValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final XmlValidationService xmlValidationService;
    private final OktaService oktaService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            if (oktaService.isPublicApiMode()) {
                return ResponseEntity.ok(oktaService.getApplications());
            }
            return ResponseEntity.ok(applicationService.findAll());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            if (oktaService.isPublicApiMode()) {
                return ResponseEntity.ok(oktaService.getApplication(id));
            }
            return ResponseEntity.ok(applicationService.findById(Long.valueOf(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/xml", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> createFromXml(@RequestBody String xmlContent) {
        try {
            Application application = applicationService.buildFromXml(xmlContent);
            if (oktaService.isPublicApiMode()) {
                return ResponseEntity.ok(oktaService.createApplication(application));
            }
            return ResponseEntity.ok(applicationService.save(application));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createFromJson(@RequestBody String jsonContent) {
        try {
            Application application = applicationService.buildFromJson(jsonContent);
            if (oktaService.isPublicApiMode()) {
                return ResponseEntity.ok(oktaService.createApplication(application));
            }
            return ResponseEntity.ok(applicationService.save(application));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Application application) {
        try {
            if (oktaService.isPublicApiMode()) {
                return ResponseEntity.ok(oktaService.createApplication(application));
            }
            return ResponseEntity.ok(applicationService.create(
                    application.getExternalId(),
                    application.getName(),
                    application.getLabel(),
                    application.getStatus(),
                    application.getSignOnMode()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Application application) {
        try {
            if (oktaService.isPublicApiMode()) {
                return ResponseEntity.ok(oktaService.updateApplication(id, application));
            }
            return ResponseEntity.ok(applicationService.update(Long.valueOf(id), application));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            if (oktaService.isPublicApiMode()) {
                oktaService.deleteApplication(id);
                return ResponseEntity.noContent().build();
            }
            applicationService.deleteById(Long.valueOf(id));
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/validate-xml")
    public ResponseEntity<Map<String, Object>> validateXml() {
        List<String> messages = xmlValidationService.validateGeneratedXml();
        return ResponseEntity.ok(Map.of(
                "messages", messages,
                "valid", messages.stream().noneMatch(m -> m.contains("GREŠKA"))
        ));
    }

    @GetMapping("/okta")
    public ResponseEntity<?> getOktaApplications() {
        return ResponseEntity.ok(oktaService.getApplications());
    }
}
