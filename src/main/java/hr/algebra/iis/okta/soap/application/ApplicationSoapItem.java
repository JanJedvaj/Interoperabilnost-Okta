package hr.algebra.iis.okta.soap.application;

import hr.algebra.iis.okta.application.xml.ApplicationXmlSearchItem;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Application", propOrder = {
        "resourceId",
        "localId",
        "externalId",
        "name",
        "label",
        "status",
        "signOnMode",
        "createdAt",
        "updatedAt"
})
public class ApplicationSoapItem {

    @XmlElement(required = true)
    private String resourceId;
    private Long localId;
    private String externalId;
    @XmlElement(required = true)
    private String name;
    @XmlElement(required = true)
    private String label;
    @XmlElement(required = true)
    private String status;
    @XmlElement(required = true)
    private String signOnMode;
    private String createdAt;
    private String updatedAt;

    public ApplicationSoapItem() {
    }

    public ApplicationSoapItem(ApplicationXmlSearchItem item) {
        resourceId = item.resourceId();
        localId = item.localId();
        externalId = item.externalId();
        name = item.name();
        label = item.label();
        status = item.status();
        signOnMode = item.signOnMode();
        createdAt = item.createdAt();
        updatedAt = item.updatedAt();
    }

    public String getResourceId() {
        return resourceId;
    }

    public Long getLocalId() {
        return localId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    public String getSignOnMode() {
        return signOnMode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
