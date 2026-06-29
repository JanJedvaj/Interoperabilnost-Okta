package hr.algebra.iis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.iis.model.Application;
import hr.algebra.iis.repository.ApplicationRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ValidationService validationService;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public Application saveFromXml(String xmlContent) throws Exception {
        List<String> errors = validationService.validateXml(xmlContent);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("XML validacija neuspješna: " + String.join(", ", errors));
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(xmlContent)));

        return applicationRepository.save(Application.builder()
                .externalId(getText(doc, "externalId"))
                .name(getText(doc, "name"))
                .label(getText(doc, "label"))
                .status(getText(doc, "status"))
                .signOnMode(getText(doc, "signOnMode"))
                .build());
    }

    public Application saveFromJson(String jsonContent) throws Exception {
        List<String> errors = validationService.validateJson(jsonContent);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("JSON validacija neuspješna: " + String.join(", ", errors));
        }

        Map<String, String> data = objectMapper.readValue(jsonContent, Map.class);
        return applicationRepository.save(Application.builder()
                .externalId(data.get("externalId"))
                .name(data.get("name"))
                .label(data.get("label"))
                .status(data.get("status"))
                .signOnMode(data.get("signOnMode"))
                .build());
    }

    public Application buildFromXml(String xmlContent) throws Exception {
        List<String> errors = validationService.validateXml(xmlContent);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("XML validacija neuspjesna: " + String.join(", ", errors));
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(xmlContent)));

        return Application.builder()
                .externalId(getText(doc, "externalId"))
                .name(getText(doc, "name"))
                .label(getText(doc, "label"))
                .status(getText(doc, "status"))
                .signOnMode(getText(doc, "signOnMode"))
                .build();
    }

    public Application buildFromJson(String jsonContent) throws Exception {
        List<String> errors = validationService.validateJson(jsonContent);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("JSON validacija neuspjesna: " + String.join(", ", errors));
        }

        Map<String, String> data = objectMapper.readValue(jsonContent, Map.class);
        return Application.builder()
                .externalId(data.get("externalId"))
                .name(data.get("name"))
                .label(data.get("label"))
                .status(data.get("status"))
                .signOnMode(data.get("signOnMode"))
                .build();
    }

    public Application save(Application application) {
        return applicationRepository.save(application);
    }

    public Application create(String externalId, String name, String label, String status, String signOnMode) {
        return applicationRepository.save(Application.builder()
                .externalId(externalId)
                .name(name)
                .label(label)
                .status(status)
                .signOnMode(signOnMode)
                .build());
    }

    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aplikacija s ID-om " + id + " nije pronađena"));
    }

    public String generateApplicationsXml() throws Exception {
        List<Application> applications = applicationRepository.findAll();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("applications");
        doc.appendChild(root);

        for (Application a : applications) {
            Element appEl = doc.createElement("application");
            appEl.setAttribute("id", String.valueOf(a.getId()));
            addElement(doc, appEl, "externalId", a.getExternalId());
            addElement(doc, appEl, "name", a.getName());
            addElement(doc, appEl, "label", a.getLabel());
            addElement(doc, appEl, "status", a.getStatus());
            addElement(doc, appEl, "signOnMode", a.getSignOnMode());
            root.appendChild(appEl);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private void addElement(Document doc, Element parent, String tagName, String value) {
        Element el = doc.createElement(tagName);
        el.setTextContent(value == null ? "" : value);
        parent.appendChild(el);
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        applicationRepository.deleteById(id);
        applicationRepository.flush();

        if (applicationRepository.count() == 0) {
            entityManager
                    .createNativeQuery("ALTER TABLE applications ALTER COLUMN id RESTART WITH 1")
                    .executeUpdate();
        }
    }

    public Application update(Long id, Application updated) {
        Application application = findById(id);
        application.setExternalId(updated.getExternalId());
        application.setName(updated.getName());
        application.setLabel(updated.getLabel());
        application.setStatus(updated.getStatus());
        application.setSignOnMode(updated.getSignOnMode());
        return applicationRepository.save(application);
    }

    private String getText(Document doc, String tagName) {
        return doc.getElementsByTagName(tagName).getLength() > 0
                ? doc.getElementsByTagName(tagName).item(0).getTextContent()
                : null;
    }
}
