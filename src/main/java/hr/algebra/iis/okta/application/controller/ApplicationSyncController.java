package hr.algebra.iis.okta.application.controller;

import hr.algebra.iis.okta.application.sync.ApplicationSyncResult;
import hr.algebra.iis.okta.application.sync.ApplicationSyncService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications/sync")
public class ApplicationSyncController {

    private final ApplicationSyncService applicationSyncService;

    public ApplicationSyncController(ApplicationSyncService applicationSyncService) {
        this.applicationSyncService = applicationSyncService;
    }

    @PostMapping("/okta")
    public ApplicationSyncResult syncFromOkta(@RequestParam(required = false) Integer limit) {
        return applicationSyncService.syncFromOkta(limit);
    }
}
