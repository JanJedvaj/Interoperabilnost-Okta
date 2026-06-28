package hr.algebra.iis.okta.okta.controller;

import hr.algebra.iis.okta.okta.client.OktaApplicationsClient;
import hr.algebra.iis.okta.okta.dto.OktaApplicationRequest;
import hr.algebra.iis.okta.okta.dto.OktaApplicationResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/okta/applications")
public class OktaApplicationsController {

    private final OktaApplicationsClient oktaApplicationsClient;

    public OktaApplicationsController(OktaApplicationsClient oktaApplicationsClient) {
        this.oktaApplicationsClient = oktaApplicationsClient;
    }

    @GetMapping
    public List<OktaApplicationResponse> listApplications(@RequestParam(required = false) Integer limit) {
        return oktaApplicationsClient.listApplications(limit);
    }

    @GetMapping("/{id}")
    public OktaApplicationResponse getApplication(@PathVariable String id) {
        return oktaApplicationsClient.getApplication(id);
    }

    @PostMapping
    public OktaApplicationResponse createApplication(@Valid @RequestBody OktaApplicationRequest request) {
        return oktaApplicationsClient.createApplication(request);
    }

    @PutMapping("/{id}")
    public OktaApplicationResponse replaceApplication(
            @PathVariable String id,
            @Valid @RequestBody OktaApplicationRequest request
    ) {
        return oktaApplicationsClient.replaceApplication(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        oktaApplicationsClient.deleteApplication(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
