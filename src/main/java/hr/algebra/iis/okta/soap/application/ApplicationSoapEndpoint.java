package hr.algebra.iis.okta.soap.application;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@WebService(
        name = "ApplicationSearchPort",
        targetNamespace = "https://algebra.hr/iis/okta/applications"
)
public interface ApplicationSoapEndpoint {

    @WebMethod(operationName = "searchApplications")
    @WebResult(name = "ApplicationSearchResponse")
    ApplicationSoapSearchResponse searchApplications(
            @WebParam(name = "searchTerm") String searchTerm
    );
}
