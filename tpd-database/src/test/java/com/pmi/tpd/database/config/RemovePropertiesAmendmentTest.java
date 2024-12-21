package com.pmi.tpd.database.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import com.pmi.tpd.api.ApplicationConstants.Setup;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * ConfigurationLineProcessor + property remover/commenter test See also {@link DataSourceConfigurationAmendmentTest}
 * for more detailed DataSource tests.
 */
public class RemovePropertiesAmendmentTest extends MockitoTestCase {

    private static Set<String> SETUP_PROPERTIES = ImmutableSet.of(Setup.SETUP_BASE_URL,
        Setup.SETUP_DISPLAY_NAME,
        Setup.SETUP_USER_NAME,
        Setup.SETUP_USER_PASSWORD,
        Setup.SETUP_USER_DISPLAY_NAME,
        Setup.SETUP_USER_EMAIL_ADDRESS);

    @Mock
    private IClock clock;

    @Test
    public void testWithLineProcessor() throws Exception {
        final DateTime dateTime = new DateTime();
        when(clock.now()).thenReturn(dateTime);

        final String unaffectedProperty = "setup.unaffected.property=some value";
        final String sysAdminPswProperty = "setup.sysadmin.password=adminPsw";

        final String fullFile = "setup.baseUrl=http://baseurl\n" + "setup.displayName=displayName\n"
                + "setup.sysadmin.username=admin\n" + sysAdminPswProperty + "\n"
                + "setup.sysadmin.displayName=Admin dispName\n" + "setup.sysadmin.emailAddress=fschroder@company.com\n"
                + unaffectedProperty + "\n";

        final CharSource inputSupplier = new CharSource() {

            @Override
            public Reader openStream() throws IOException {
                return new StringReader(fullFile);
            }

        };

        final StringWriter stringWriter = new StringWriter();
        final BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        final ConfigurationLineProcessor configurationLineProcessor = new ConfigurationLineProcessor(bufferedWriter,
                new RemovePropertiesAmendment(clock, SETUP_PROPERTIES));
        inputSupplier.readLines(configurationLineProcessor);

        bufferedWriter.close();
        final String modifiedConfigFileContent = stringWriter.toString();

        assertTrue(modifiedConfigFileContent.contains(commentedProperty(Setup.SETUP_BASE_URL, clock)));
        assertTrue(modifiedConfigFileContent.contains(commentedProperty(Setup.SETUP_DISPLAY_NAME, clock)));
        assertTrue(modifiedConfigFileContent.contains(commentedProperty(Setup.SETUP_USER_NAME, clock)));
        assertTrue(modifiedConfigFileContent.contains(commentedProperty(Setup.SETUP_USER_PASSWORD, clock)));
        assertTrue(modifiedConfigFileContent.contains(commentedProperty(Setup.SETUP_USER_DISPLAY_NAME, clock)));
        assertTrue(modifiedConfigFileContent.contains(commentedProperty(Setup.SETUP_USER_EMAIL_ADDRESS, clock)));

        assertTrue(modifiedConfigFileContent.contains(unaffectedProperty));

        assertTrue(!modifiedConfigFileContent.contains(sysAdminPswProperty));
    }

    private String commentedProperty(final String propertyKey, final IClock clock) {
        return RemovePropertiesAmendment.formatComment(propertyKey, clock);
    }
}
