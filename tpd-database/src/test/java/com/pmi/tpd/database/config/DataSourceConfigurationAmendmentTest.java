package com.pmi.tpd.database.config;

import static org.hamcrest.core.IsIterableContaining.hasItem;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.database.DatabaseConstants;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests the operation of the {@link DataSourceConfigurationAmendment} class.
 */

public class DataSourceConfigurationAmendmentTest extends MockitoTestCase {

    private static final String JDBC_URL = "jdbc:db://localhost:7990/app";

    private static final String JDBC_DRIVER = "org.db.driver.NoJdbcDriver";

    private static final String JDBC_USER = "username";

    private static final String JDBC_PASSWORD = "s3cr3t";

    private static final String PROPERTY_LINE = String.format("%s=%s", DatabaseConstants.PROP_JDBC_DRIVER, JDBC_DRIVER);

    @Mock
    private IClock clock;

    @Mock
    private IDataSourceConfiguration dataSourceConfig;

    @Mock
    private IUser user;

    @BeforeEach
    public void setUp() throws Exception {
        when(clock.now()).thenReturn(new DateTime(2004, 12, 25, 12, 0, 0, 0, DateTimeZone.UTC));

        when(dataSourceConfig.getUrl()).thenReturn(JDBC_URL);
        when(dataSourceConfig.getDriverClassName()).thenReturn(JDBC_DRIVER);
        when(dataSourceConfig.getUser()).thenReturn(JDBC_USER);
        when(dataSourceConfig.getPassword()).thenReturn(JDBC_PASSWORD);

        user = User.builder().displayName("Joe Bloggs").username("jbloggs").email("joe.blogs@company.com").build();
    }

    @Test
    public void testNoMessageWithoutUser() throws IOException {
        final DataSourceConfigurationAmendment amendment = new DataSourceConfigurationAmendment(dataSourceConfig,
                Optional.empty(), clock, null);

        final StringWriter writer = new StringWriter();
        amendment.amend(writer, PROPERTY_LINE);
        assertOutput(writer.toString());
    }

    @Test
    public void testNoMessageWithUser() throws IOException {
        final DataSourceConfigurationAmendment amendment = new DataSourceConfigurationAmendment(dataSourceConfig,
                Optional.empty(), clock, user);

        final StringWriter writer = new StringWriter();
        amendment.amend(writer, PROPERTY_LINE);
        assertOutput(user, writer.toString());
    }

    @Test
    public void testWithMessageAndUser() throws IOException {
        final String message = "Migration of database from HSQL to MySQL";
        final DataSourceConfigurationAmendment amendment = new DataSourceConfigurationAmendment(dataSourceConfig,
                Optional.of(message), clock, user);

        final StringWriter writer = new StringWriter();
        amendment.amend(writer, PROPERTY_LINE);
        assertOutput(user, message, writer.toString());
    }

    @Test
    public void testWithMessageWithoutUser() throws IOException {
        final String message = "Migration of database from HSQL to MySQL";
        final DataSourceConfigurationAmendment amendment = new DataSourceConfigurationAmendment(dataSourceConfig,
                Optional.of(message), clock, null);

        final StringWriter writer = new StringWriter();
        amendment.amend(writer, PROPERTY_LINE);
        assertOutput(message, writer.toString());
    }

    // The properties code considers \ and escape character, so strings like
    // "some.host\instance" become corrupted
    // if the backslash is not escaped when the value is written
    @Test
    public void testWriteToEscapesBackslashes() throws IOException {
        reset(dataSourceConfig);

        when(dataSourceConfig.getUrl()).thenReturn("jdbc:app://localhost\\instance:7990/app");
        when(dataSourceConfig.getDriverClassName()).thenReturn("com\\company\\app\\NoJdbcDriver");
        when(dataSourceConfig.getUser()).thenReturn("na\\user");
        when(dataSourceConfig.getPassword()).thenReturn("abc123!@#/\\");

        final DataSourceConfigurationAmendment amendment = new DataSourceConfigurationAmendment(dataSourceConfig,
                Optional.empty(), clock, user);

        final StringWriter writer = new StringWriter();
        amendment.amend(writer, PROPERTY_LINE);
        final Iterable<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings().split(writer.toString());

        assertThat(lines, hasItem("database.jdbc.driverClassName=com\\\\company\\\\app\\\\NoJdbcDriver"));
        assertThat(lines, hasItem("database.jdbc.url=jdbc:app://localhost\\\\instance:7990/app"));
        assertThat(lines, hasItem("database.jdbc.username=na\\\\user"));
        assertThat(lines, hasItem("database.jdbc.password=abc123!@#/\\\\"));
    }

    private void assertOutput(final String output) {
        assertOutput(null, null, output);
    }

    private void assertOutput(final String message, final String output) {
        assertOutput(null, message, output);
    }

    private void assertOutput(final IUser user, final String output) {
        assertOutput(user, null, output);
    }

    private void assertOutput(final IUser user, final String message, final String output) {
        final Iterable<String> lines = Splitter.on('\n').trimResults().omitEmptyStrings().split(output);
        final Iterator<String> iterator = lines.iterator();

        assertEquals("#>*******************************************************", iterator.next());
        if (StringUtils.isNotBlank(message)) {
            assertEquals(String.format("#> %s", message), iterator.next());
        }

        if (user == null) {
            assertEquals("#> Updated on 2004-12-25T12:00:00.000Z", iterator.next());
        } else {
            assertEquals("#> Updated by Joe Bloggs on 2004-12-25T12:00:00.000Z", iterator.next());
        }

        assertEquals("#>*******************************************************", iterator.next());

        assertThat(lines, hasItem(String.format("%s=%s", DatabaseConstants.PROP_JDBC_DRIVER, JDBC_DRIVER)));
        assertThat(lines, hasItem(String.format("%s=%s", DatabaseConstants.PROP_JDBC_URL, JDBC_URL)));
        assertThat(lines, hasItem(String.format("%s=%s", DatabaseConstants.PROP_JDBC_USER, JDBC_USER)));
        assertThat(lines, hasItem(String.format("%s=%s", DatabaseConstants.PROP_JDBC_PASSWORD, JDBC_PASSWORD)));

        Iterators.advance(iterator, 4);

        assertEquals(String.format("# %s", PROPERTY_LINE), iterator.next());
    }
}
