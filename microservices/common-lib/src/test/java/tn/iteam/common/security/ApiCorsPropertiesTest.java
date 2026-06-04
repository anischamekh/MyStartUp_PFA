package tn.iteam.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ApiCorsPropertiesTest {

    @Test
    void setAllowedOriginsFromCommaSeparated() {
        ApiCorsProperties props = new ApiCorsProperties();
        props.setAllowedOrigins("http://a.example, http://b.example");
        assertEquals(List.of("http://a.example", "http://b.example"), props.getAllowedOrigins());
    }

    @Test
    void enabledFlagCanBeToggled() {
        ApiCorsProperties props = new ApiCorsProperties();
        assertTrue(props.isEnabled());
        props.setEnabled(false);
        assertFalse(props.isEnabled());
    }
}
