package hr.algebra.iis.okta.application.validation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class XmlApplicationImportValidator {

    private static final String SCHEMA_PATH = "schemas/application.xsd";

    private final Schema schema;

    public XmlApplicationImportValidator() throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try (InputStream inputStream = new ClassPathResource(SCHEMA_PATH).getInputStream()) {
            schema = schemaFactory.newSchema(new StreamSource(inputStream));
        }
    }

    public void validate(String xmlDocument) {
        Validator validator = schema.newValidator();
        CollectingXmlErrorHandler errorHandler = new CollectingXmlErrorHandler();
        validator.setErrorHandler(errorHandler);

        try {
            validator.validate(new StreamSource(new StringReader(xmlDocument)));

            if (!errorHandler.getErrors().isEmpty()) {
                throw new ApplicationImportValidationException(errorHandler.getErrors());
            }
        } catch (SAXException | IOException exception) {
            List<String> errors = errorHandler.getErrors();
            if (errors.isEmpty()) {
                errors = List.of(exception.getMessage());
            }

            throw new ApplicationImportValidationException(errors);
        }
    }

    private static final class CollectingXmlErrorHandler implements org.xml.sax.ErrorHandler {

        private final List<String> errors = new ArrayList<>();

        @Override
        public void warning(SAXParseException exception) {
            errors.add(format("Warning", exception));
        }

        @Override
        public void error(SAXParseException exception) {
            errors.add(format("Error", exception));
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            errors.add(format("Fatal error", exception));
            throw exception;
        }

        List<String> getErrors() {
            return List.copyOf(errors);
        }

        private static String format(String severity, SAXParseException exception) {
            return "%s at line %d, column %d: %s".formatted(
                    severity,
                    exception.getLineNumber(),
                    exception.getColumnNumber(),
                    exception.getMessage()
            );
        }
    }
}
