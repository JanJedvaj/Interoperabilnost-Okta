package hr.algebra.iis.okta.application.xml;

import hr.algebra.iis.okta.application.dto.ApplicationResponse;
import hr.algebra.iis.okta.application.provider.ApplicationProvider;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationXmlSearchService {

    private static final String SEARCH_EXPRESSION = """
            /applications/application[
              contains(resourceId, $term)
              or contains(externalId, $term)
              or contains(name, $term)
              or contains(label, $term)
              or contains(status, $term)
              or contains(signOnMode, $term)
            ]
            """;

    private final ApplicationProvider applicationProvider;
    private final ApplicationXmlDocumentService applicationXmlDocumentService;

    public ApplicationXmlSearchService(
            ApplicationProvider applicationProvider,
            ApplicationXmlDocumentService applicationXmlDocumentService
    ) {
        this.applicationProvider = applicationProvider;
        this.applicationXmlDocumentService = applicationXmlDocumentService;
    }

    public ApplicationXmlSearchResult search(String searchTerm) {
        String normalizedTerm = searchTerm == null ? "" : searchTerm.trim();
        List<ApplicationResponse> applications = applicationProvider.findAll();
        ApplicationXmlDocument snapshot = applicationXmlDocumentService.createSnapshot(applications);

        List<ApplicationXmlSearchItem> matches = snapshot.validationResult().valid()
                ? findMatches(snapshot.xml(), normalizedTerm)
                : List.of();

        return new ApplicationXmlSearchResult(
                normalizedTerm,
                snapshot.xml(),
                snapshot.validationResult().valid(),
                snapshot.validationResult().messages(),
                matches
        );
    }

    private List<ApplicationXmlSearchItem> findMatches(String xml, String searchTerm) {
        try {
            Document document = parse(xml);
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setXPathVariableResolver(variableName -> resolveSearchTerm(variableName, searchTerm));

            NodeList nodes = (NodeList) xpath.evaluate(SEARCH_EXPRESSION, document, XPathConstants.NODESET);
            List<ApplicationXmlSearchItem> matches = new ArrayList<>();
            for (int index = 0; index < nodes.getLength(); index++) {
                matches.add(toSearchItem((Element) nodes.item(index)));
            }
            return matches;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException exception) {
            throw new ApplicationXmlException("Unable to search applications XML snapshot with XPath", exception);
        }
    }

    private static Object resolveSearchTerm(QName variableName, String searchTerm) {
        if ("term".equals(variableName.getLocalPart())) {
            return searchTerm;
        }
        return "";
    }

    private static Document parse(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = secureDocumentBuilderFactory();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(new StringReader(xml)));
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

    private static ApplicationXmlSearchItem toSearchItem(Element element) {
        return new ApplicationXmlSearchItem(
                requiredText(element, "resourceId"),
                optionalLong(element, "localId"),
                optionalText(element, "externalId"),
                requiredText(element, "name"),
                requiredText(element, "label"),
                requiredText(element, "status"),
                requiredText(element, "signOnMode"),
                optionalText(element, "createdAt"),
                optionalText(element, "updatedAt")
        );
    }

    private static String requiredText(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private static String optionalText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }

    private static Long optionalLong(Element element, String tagName) {
        String value = optionalText(element, tagName);
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
