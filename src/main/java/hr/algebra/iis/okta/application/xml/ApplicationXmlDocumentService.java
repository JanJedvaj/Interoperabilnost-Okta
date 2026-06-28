package hr.algebra.iis.okta.application.xml;

import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ApplicationXmlDocumentService {

    private final ApplicationXmlValidator applicationXmlValidator;

    public ApplicationXmlDocumentService(ApplicationXmlValidator applicationXmlValidator) {
        this.applicationXmlValidator = applicationXmlValidator;
    }

    public ApplicationXmlDocument createSnapshot(List<ApplicationResponse> applications) {
        String xml = toXml(applications);
        return new ApplicationXmlDocument(xml, applicationXmlValidator.validate(xml));
    }

    private String toXml(List<ApplicationResponse> applications) {
        try {
            DocumentBuilderFactory documentBuilderFactory = secureDocumentBuilderFactory();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("applications");
            root.setAttribute("count", String.valueOf(applications.size()));
            document.appendChild(root);

            for (ApplicationResponse application : applications) {
                root.appendChild(toElement(document, application));
            }

            return serialize(document);
        } catch (ParserConfigurationException | TransformerException exception) {
            throw new ApplicationXmlException("Unable to generate applications XML snapshot", exception);
        }
    }

    private static Element toElement(Document document, ApplicationResponse application) {
        Element applicationElement = document.createElement("application");
        appendText(document, applicationElement, "resourceId", application.resourceId());
        appendOptionalText(document, applicationElement, "localId", application.id());
        appendOptionalText(document, applicationElement, "externalId", application.externalId());
        appendText(document, applicationElement, "name", application.name());
        appendText(document, applicationElement, "label", application.label());
        appendText(document, applicationElement, "status", application.status());
        appendText(document, applicationElement, "signOnMode", application.signOnMode());
        appendOptionalText(document, applicationElement, "createdAt", application.createdAt());
        appendOptionalText(document, applicationElement, "updatedAt", application.updatedAt());
        return applicationElement;
    }

    private static void appendText(Document document, Element parent, String name, String value) {
        Element element = document.createElement(name);
        element.setTextContent(value == null ? "" : value);
        parent.appendChild(element);
    }

    private static void appendOptionalText(Document document, Element parent, String name, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof OffsetDateTime dateTime) {
            appendText(document, parent, name, dateTime.toString());
            return;
        }

        appendText(document, parent, name, value.toString());
    }

    private static DocumentBuilderFactory secureDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static String serialize(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }
}
