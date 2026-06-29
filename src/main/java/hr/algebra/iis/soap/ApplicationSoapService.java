package hr.algebra.iis.soap;

import hr.algebra.iis.service.ApplicationService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

@WebService(serviceName = "ApplicationService")
@Component
@RequiredArgsConstructor
public class ApplicationSoapService {

    private final ApplicationService applicationService;

    @WebMethod(operationName = "searchApplications")
    public String searchApplications(@WebParam(name = "keyword") String keyword) {
        try {
            String xmlContent = applicationService.generateApplicationsXml();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            String normalized = keyword == null ? "" : keyword.toLowerCase();
            String expression = "//application[contains(translate(name, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                    + normalized + "') or contains(translate(label, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                    + normalized + "') or contains(translate(status, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                    + normalized + "') or contains(translate(signOnMode, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                    + normalized + "')]";

            NodeList results = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

            StringBuilder sb = new StringBuilder("<searchResults>");
            sb.append("<keyword>").append(keyword).append("</keyword>");
            sb.append("<count>").append(results.getLength()).append("</count>");

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            for (int i = 0; i < results.getLength(); i++) {
                Node node = results.item(i);
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(node), new StreamResult(writer));
                sb.append(writer);
            }
            sb.append("</searchResults>");
            return sb.toString();

        } catch (Exception e) {
            return "<error>" + e.getMessage() + "</error>";
        }
    }
}
