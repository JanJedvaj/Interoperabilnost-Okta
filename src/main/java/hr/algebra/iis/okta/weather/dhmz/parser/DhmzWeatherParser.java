package hr.algebra.iis.okta.weather.dhmz.parser;

import hr.algebra.iis.okta.weather.dhmz.dto.DhmzWeatherObservation;
import hr.algebra.iis.okta.weather.dhmz.exception.DhmzWeatherException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DhmzWeatherParser {

    public List<DhmzWeatherObservation> parse(String xml) {
        try {
            DocumentBuilder documentBuilder = secureDocumentBuilderFactory().newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));
            NodeList cityNodes = document.getElementsByTagName("Grad");

            List<DhmzWeatherObservation> observations = new ArrayList<>();
            for (int index = 0; index < cityNodes.getLength(); index++) {
                Node node = cityNodes.item(index);
                if (node instanceof Element cityElement) {
                    observations.add(toObservation(cityElement));
                }
            }
            return observations;
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new DhmzWeatherException("Unable to parse DHMZ observations XML", exception);
        }
    }

    private static DhmzWeatherObservation toObservation(Element cityElement) {
        return new DhmzWeatherObservation(
                firstText(cityElement, "GradIme", "Ime", "Naziv"),
                firstText(cityElement, "Temp", "Temperatura"),
                firstText(cityElement, "Vlaga"),
                firstText(cityElement, "Tlak"),
                firstText(cityElement, "TlakTend"),
                firstText(cityElement, "VjetarSmjer"),
                firstText(cityElement, "VjetarBrzina"),
                firstText(cityElement, "Vrijeme")
        );
    }

    private static String firstText(Element element, String... tagNames) {
        for (String tagName : tagNames) {
            NodeList nodes = element.getElementsByTagName(tagName);
            if (nodes.getLength() > 0 && nodes.item(0) != null) {
                String value = nodes.item(0).getTextContent();
                if (value != null) {
                    return value.trim();
                }
            }
        }
        return "";
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
}
