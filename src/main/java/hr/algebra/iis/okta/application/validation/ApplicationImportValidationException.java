package hr.algebra.iis.okta.application.validation;

import java.util.List;

public class ApplicationImportValidationException extends RuntimeException {

    private final List<String> validationErrors;

    public ApplicationImportValidationException(List<String> validationErrors) {
        super("Application import document is not valid");
        this.validationErrors = List.copyOf(validationErrors);
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
