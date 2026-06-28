package hr.algebra.iis.okta.application;

import hr.algebra.iis.okta.application.controller.ApplicationImportController;
import hr.algebra.iis.okta.application.repository.ApplicationRepository;
import hr.algebra.iis.okta.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:application_import_controller_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ApplicationImportControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ApplicationImportController applicationImportController;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void cleanDatabase() {
        applicationRepository.deleteAll();
        mockMvc = MockMvcBuilders
                .standaloneSetup(applicationImportController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    void importsValidJsonDocument() throws Exception {
        mockMvc.perform(post("/api/applications/import/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalId": "okta-json-1",
                                  "name": "oidc_client",
                                  "label": "JSON Imported App",
                                  "status": "ACTIVE",
                                  "signOnMode": "OPENID_CONNECT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/applications/\\d+")))
                .andExpect(jsonPath("$.externalId").value("okta-json-1"))
                .andExpect(jsonPath("$.label").value("JSON Imported App"));

        assertEquals(1, applicationRepository.count());
    }

    @Test
    void rejectsInvalidJsonDocumentBeforeSaving() throws Exception {
        mockMvc.perform(post("/api/applications/import/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "externalId": "okta-json-invalid",
                                  "name": "",
                                  "status": "ACTIVE",
                                  "signOnMode": "OPENID_CONNECT",
                                  "unexpected": "not allowed"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application import document is not valid"))
                .andExpect(jsonPath("$.validationErrors", hasSize(3)));

        assertEquals(0, applicationRepository.count());
    }

    @Test
    void importsValidXmlDocument() throws Exception {
        mockMvc.perform(post("/api/applications/import/xml")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("""
                                <?xml version="1.0" encoding="UTF-8"?>
                                <application>
                                    <externalId>okta-xml-1</externalId>
                                    <name>saml_app</name>
                                    <label>XML Imported App</label>
                                    <status>ACTIVE</status>
                                    <signOnMode>SAML_2_0</signOnMode>
                                </application>
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value("okta-xml-1"))
                .andExpect(jsonPath("$.name").value("saml_app"))
                .andExpect(jsonPath("$.label").value("XML Imported App"))
                .andExpect(jsonPath("$.signOnMode").value("SAML_2_0"));

        assertEquals(1, applicationRepository.count());
    }

    @Test
    void rejectsInvalidXmlDocumentBeforeSaving() throws Exception {
        mockMvc.perform(post("/api/applications/import/xml")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("""
                                <?xml version="1.0" encoding="UTF-8"?>
                                <application>
                                    <externalId>okta-xml-invalid</externalId>
                                    <name></name>
                                    <status>ACTIVE</status>
                                    <signOnMode>SAML_2_0</signOnMode>
                                </application>
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application import document is not valid"))
                .andExpect(jsonPath("$.validationErrors").isArray());

        assertEquals(0, applicationRepository.count());
    }

    @Test
    void rejectsXmlWithDoctypeBeforeSaving() throws Exception {
        mockMvc.perform(post("/api/applications/import/xml")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("""
                                <?xml version="1.0" encoding="UTF-8"?>
                                <!DOCTYPE application [
                                  <!ENTITY xxe SYSTEM "file:///etc/passwd">
                                ]>
                                <application>
                                    <externalId>okta-xml-xxe</externalId>
                                    <name>&xxe;</name>
                                    <label>Unsafe XML</label>
                                    <status>ACTIVE</status>
                                    <signOnMode>SAML_2_0</signOnMode>
                                </application>
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application import document is not valid"));

        assertEquals(0, applicationRepository.count());
    }
}
