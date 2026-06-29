package hr.algebra.iis.okta.graphql.application;

public record DeleteApplicationPayload(
        boolean deleted,
        String id
) {

    static DeleteApplicationPayload deleted(String id) {
        return new DeleteApplicationPayload(true, id);
    }
}
