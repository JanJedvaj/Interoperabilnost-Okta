package hr.algebra.iis.okta.soap.application;

import hr.algebra.iis.okta.application.xml.ApplicationXmlSearchResult;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

@XmlRootElement(name = "ApplicationSearchResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationSearchResponse", propOrder = {
        "searchTerm",
        "xmlValid",
        "validationMessages",
        "applications"
})
public class ApplicationSoapSearchResponse {

    @XmlElement(required = true)
    private String searchTerm;

    private boolean xmlValid;

    @XmlElementWrapper(name = "validationMessages")
    @XmlElement(name = "message")
    private List<String> validationMessages;

    @XmlElementWrapper(name = "applications")
    @XmlElement(name = "application")
    private List<ApplicationSoapItem> applications;

    public ApplicationSoapSearchResponse() {
    }

    public ApplicationSoapSearchResponse(ApplicationXmlSearchResult result) {
        searchTerm = result.searchTerm();
        xmlValid = result.xmlValid();
        validationMessages = result.validationMessages();
        applications = result.applications()
                .stream()
                .map(ApplicationSoapItem::new)
                .toList();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public boolean isXmlValid() {
        return xmlValid;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public List<ApplicationSoapItem> getApplications() {
        return applications;
    }
}
