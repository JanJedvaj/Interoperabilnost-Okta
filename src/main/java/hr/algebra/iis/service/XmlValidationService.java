package hr.algebra.iis.service;

import hr.algebra.iis.model.Application;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class XmlValidationService {

    private final Validator validator;
    private final ApplicationService applicationService;

    public List<String> validateGeneratedXml() {
        List<String> messages = new ArrayList<>();
        try {
            String xmlContent = applicationService.generateApplicationsXml();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            NodeList applicationNodes = doc.getElementsByTagName("application");

            if (applicationNodes.getLength() == 0) {
                messages.add("UPOZORENJE: XML ne sadrži niti jednu aplikaciju");
                return messages;
            }

            for (int i = 0; i < applicationNodes.getLength(); i++) {
                Element appEl = (Element) applicationNodes.item(i);
                String id = appEl.getAttribute("id");

                Application application = Application.builder()
                        .externalId(getElementText(appEl, "externalId"))
                        .name(getElementText(appEl, "name"))
                        .label(getElementText(appEl, "label"))
                        .status(getElementText(appEl, "status"))
                        .signOnMode(getElementText(appEl, "signOnMode"))
                        .build();

                Set<ConstraintViolation<Application>> violations = validator.validate(application);
                if (violations.isEmpty()) {
                    messages.add("Aplikacija ID=" + id + ": VALIDNA");
                } else {
                    for (ConstraintViolation<Application> v : violations) {
                        messages.add("Aplikacija ID=" + id + " - GREŠKA: " + v.getPropertyPath() + " " + v.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            messages.add("Greška pri validaciji: " + e.getMessage());
        }
        return messages;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }
}
