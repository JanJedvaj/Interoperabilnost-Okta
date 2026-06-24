package hr.algebra.iis.okta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InteroperabilityOktaApplicationTests {

    @Test
    void applicationEntryPointCanBeLoaded() {
        assertDoesNotThrow(() -> Class.forName(InteroperabilityOktaApplication.class.getName()));
    }
}

