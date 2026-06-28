package hr.algebra.iis.okta.okta.exception;

public class OktaApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public OktaApiException(int statusCode, String responseBody) {
        super("Okta API request failed with status %d".formatted(statusCode));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
