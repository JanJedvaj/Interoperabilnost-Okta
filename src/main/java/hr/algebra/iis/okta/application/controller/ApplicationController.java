package hr.algebra.iis.okta.application.controller;

import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.provider.ApplicationProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationProvider applicationProvider;

    public ApplicationController(ApplicationProvider applicationProvider) {
        this.applicationProvider = applicationProvider;
    }

    @GetMapping
    public List<ApplicationResponse> findAll() {
        return applicationProvider.findAll();
    }

    @GetMapping("/{id}")
    public ApplicationResponse findById(@PathVariable String id) {
        return applicationProvider.findById(id);
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationProvider.create(request);
        return ResponseEntity
                .created(URI.create("/api/applications/" + response.resourceId()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable String id, @Valid @RequestBody ApplicationRequest request) {
        return applicationProvider.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        applicationProvider.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
