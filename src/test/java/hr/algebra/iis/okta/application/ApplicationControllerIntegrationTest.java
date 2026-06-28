package hr.algebra.iis.okta.application;

import hr.algebra.iis.okta.application.controller.ApplicationController;
import hr.algebra.iis.okta.application.model.ApplicationEntity;
import hr.algebra.iis.okta.application.repository.ApplicationRepository;
import hr.algebra.iis.okta.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:application_controller_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApplicationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ApplicationController applicationController;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void cleanDatabase() {
        applicationRepository.deleteAll();
        mockMvc = MockMvcBuilders
                .standaloneSetup(applicationController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    void createsApplication() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validApplicationJson("okta-app-1", "oidc_client", "CRM Portal")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/applications/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.externalId").value("okta-app-1"))
                .andExpect(jsonPath("$.name").value("oidc_client"))
                .andExpect(jsonPath("$.label").value("CRM Portal"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.signOnMode").value("OPENID_CONNECT"));
    }

    @Test
    void returnsAllApplications() throws Exception {
        applicationRepository.save(new ApplicationEntity("okta-app-1", "oidc_client", "CRM Portal", "ACTIVE", "OPENID_CONNECT"));
        applicationRepository.save(new ApplicationEntity("okta-app-2", "saml_app", "Partner SSO", "INACTIVE", "SAML_2_0"));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label").value("CRM Portal"))
                .andExpect(jsonPath("$[1].label").value("Partner SSO"));
    }

    @Test
    void returnsApplicationById() throws Exception {
        ApplicationEntity saved = applicationRepository.save(
                new ApplicationEntity("okta-app-1", "oidc_client", "CRM Portal", "ACTIVE", "OPENID_CONNECT")
        );

        mockMvc.perform(get("/api/applications/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.label").value("CRM Portal"));
    }

    @Test
    void updatesApplication() throws Exception {
        ApplicationEntity saved = applicationRepository.save(
                new ApplicationEntity("okta-app-1", "oidc_client", "CRM Portal", "ACTIVE", "OPENID_CONNECT")
        );

        mockMvc.perform(put("/api/applications/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validApplicationJson("okta-app-1", "bookmark_app", "Internal Wiki")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("bookmark_app"))
                .andExpect(jsonPath("$.label").value("Internal Wiki"));
    }

    @Test
    void deletesApplication() throws Exception {
        ApplicationEntity saved = applicationRepository.save(
                new ApplicationEntity("okta-app-1", "oidc_client", "CRM Portal", "ACTIVE", "OPENID_CONNECT")
        );

        mockMvc.perform(delete("/api/applications/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/applications/{id}", saved.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Application with id %d was not found".formatted(saved.getId())));
    }

    @Test
    void returnsValidationErrorsForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "label": "",
                                  "status": "",
                                  "signOnMode": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"))
                .andExpect(jsonPath("$.fieldErrors.label").value("Label is required"))
                .andExpect(jsonPath("$.fieldErrors.status").value("Status is required"))
                .andExpect(jsonPath("$.fieldErrors.signOnMode").value("Sign-on mode is required"));
    }

    @Test
    void returnsNotFoundForMissingApplication() throws Exception {
        mockMvc.perform(get("/api/applications/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/applications/404"));
    }

    private static String validApplicationJson(String externalId, String name, String label) {
        return """
                {
                  "externalId": "%s",
                  "name": "%s",
                  "label": "%s",
                  "status": "ACTIVE",
                  "signOnMode": "OPENID_CONNECT"
                }
                """.formatted(externalId, name, label);
    }
}
