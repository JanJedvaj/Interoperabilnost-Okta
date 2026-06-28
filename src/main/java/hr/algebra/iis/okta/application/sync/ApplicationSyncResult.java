package hr.algebra.iis.okta.application.sync;

public record ApplicationSyncResult(
        int receivedFromOkta,
        int savedLocally
) {
}
