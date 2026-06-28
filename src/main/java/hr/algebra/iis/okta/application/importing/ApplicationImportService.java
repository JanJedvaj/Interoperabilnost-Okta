package hr.algebra.iis.okta.application.importing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.iis.okta.application.dto.ApplicationRequest;
import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.service.ApplicationService;
import hr.algebra.iis.okta.application.validation.ApplicationImportValidationException;
import hr.algebra.iis.okta.application.validation.JsonApplicationImportValidator;
import hr.algebra.iis.okta.application.validation.XmlApplicationImportValidator;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@Service
public class ApplicationImportService {

    private final ApplicationService applicationService;
    private final JsonApplicationImportValidator jsonValidator;
    private final XmlApplicationImportValidator xmlValidator;
    private final ObjectMapper objectMapper;

    public ApplicationImportService(
            ApplicationService applicationService,
            JsonApplicationImportValidator jsonValidator,
            XmlApplicationImportValidator xmlValidator,
            ObjectMapper objectMapper
    ) {
        this.applicationService = applicationService;
        this.jsonValidator = jsonValidator;
        this.xmlValidator = xmlValidator;
        this.objectMapper = objectMapper;
    }

    public ApplicationResponse importJson(String document) {
        JsonNode jsonDocument;

        try {
            jsonDocument = objectMapper.readTree(document);
        } catch (JsonProcessingException exception) {
            throw new ApplicationImportValidationException(List.of(exception.getOriginalMessage()));
        }

        return importJson(jsonDocument);
    }

    private ApplicationResponse importJson(JsonNode document) {
        jsonValidator.validate(document);

        try {
            ApplicationRequest request = objectMapper.treeToValue(document, ApplicationRequest.class);
            return applicationService.create(request);
        } catch (JsonProcessingException exception) {
            throw new ApplicationImportValidationException(List.of(exception.getOriginalMessage()));
        }
    }

    public ApplicationResponse importXml(String document) {
        xmlValidator.validate(document);
        return applicationService.create(toApplicationRequest(document));
    }

    private ApplicationRequest toApplicationRequest(String xmlDocument) {
        try {
            DocumentBuilderFactory factory = secureDocumentBuilderFactory();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(xmlDocument)));
            Element root = document.getDocumentElement();

            return new ApplicationRequest(
                    optionalText(root, "externalId"),
                    requiredText(root, "name"),
                    requiredText(root, "label"),
                    requiredText(root, "status"),
                    requiredText(root, "signOnMode")
            );
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new ApplicationImportValidationException(List.of(exception.getMessage()));
        }
    }

    private static DocumentBuilderFactory secureDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private static String requiredText(Element root, String tagName) {
        return root.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private static String optionalText(Element root, String tagName) {
        if (root.getElementsByTagName(tagName).getLength() == 0) {
            return null;
        }

        return root.getElementsByTagName(tagName).item(0).getTextContent();
    }
}
