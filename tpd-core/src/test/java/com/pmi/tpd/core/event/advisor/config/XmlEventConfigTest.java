package com.pmi.tpd.core.event.advisor.config;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.event.EventType;
import com.pmi.tpd.core.event.advisor.SimpleApplicationEventCheck;
import com.pmi.tpd.core.event.advisor.SimpleEventCheck;
import com.pmi.tpd.core.event.advisor.SimpleRequestEventCheck;
import com.pmi.tpd.core.event.advisor.SimpleSetupConfig;
import com.pmi.tpd.testing.junit5.TestCase;

public class XmlEventConfigTest extends TestCase {

    @Test
    public void testBadEventCheck() {
        assertThrows(ConfigurationEventException.class, () -> {
            XmlEventConfig.fromFile(getPackagePath() + "/test-event-config-badeventcheck.xml");
        });
    }

    @Test
    public void testBadEventCheckId() {
        assertThrows(ConfigurationEventException.class, () -> {
            XmlEventConfig.fromFile(getPackagePath() + "/test-event-config-badid.xml");
        });
    }

    @Test
    public void testDuplicateEventCheckId() {
        assertThrows(ConfigurationEventException.class, () -> {
            XmlEventConfig.fromFile(getPackagePath() + "/test-event-config-duplicateid.xml");
        });
    }

    @Test
    public void testFromFile() {
        final XmlEventConfig config = XmlEventConfig.fromFile(getPackagePath() + "/test-event-config.xml");

        // parameters
        assertEquals("bar", config.getParams().get("foo"));
        assertEquals("bat", config.getParams().get("baz"));

        // setup config
        assertTrue(config.getSetupConfig() instanceof SimpleSetupConfig);

        // event checks
        assertEquals(3, config.getEventChecks().size());
        assertTrue(config.getEventChecks().get(0) instanceof SimpleEventCheck);

        assertEquals(1, config.getRequestEventChecks().size());
        assertTrue(config.getEventChecks().get(1) instanceof SimpleRequestEventCheck);

        assertEquals(1, config.getApplicationEventChecks().size());
        assertTrue(config.getEventChecks().get(2) instanceof SimpleApplicationEventCheck);

        assertTrue(config.getEventCheck(1) instanceof SimpleEventCheck);
        assertTrue(config.getEventCheck(2) instanceof SimpleRequestEventCheck);
        assertNull(config.getEventCheck(3));

        // setup and error paths
        assertEquals("/the/setup/path.jsp", config.getSetupPath());
        assertEquals("/the/error/path.jsp", config.getErrorPath());

        // ignore paths
        assertEquals(2, config.getIgnorePaths().size());
        assertTrue(config.getIgnorePaths().contains("/ignore/path/1.jsp"));
        assertTrue(config.getIgnorePaths().contains("/ignore/path/*.html"));

        // some ignore mapping tests
        assertTrue(config.isIgnoredPath("/ignore/path/1.jsp"));
        assertTrue(config.isIgnoredPath("/ignore/path/2.html"));
        assertTrue(config.isIgnoredPath("/ignore/path/foo.html"));
        assertFalse(config.isIgnoredPath("/ignore/path"));

        // event levels
        final EventLevel expectedError = new EventLevel("error", "Error");
        assertEquals(expectedError, config.getEventLevel("error"));
        final EventLevel expectedWarning = new EventLevel("warning", "This is a warning buddy");
        assertEquals(expectedWarning, config.getEventLevel("warning"));

        // event types
        final EventType expectedDatabase = new EventType("database", "Database");
        assertEquals(expectedDatabase, config.getEventType("database"));
        final EventType expectedUpgrade = new EventType("upgrade", "Upgrade");
        assertEquals(expectedUpgrade, config.getEventType("upgrade"));
    }
}
