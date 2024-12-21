package com.pmi.tpd.database.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.database.DatabaseConstants;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.testing.junit5.TestCase;

public class DataSourcePropertySerializerTest extends TestCase {

    private static String[] TEST_VALUES = new String[] { null, "", "blah", "//", "blah¡foo", "$_!d-=", "\\:" };

    private static List<Arguments> data() {
        final List<Arguments> data = Lists.newArrayList();
        // try the each of the strings defined above with every property
        for (int i = 0; i < TEST_VALUES.length; i++) {
            data.add(Arguments.of(TEST_VALUES[i % TEST_VALUES.length],
                TEST_VALUES[(i + 1) % TEST_VALUES.length],
                TEST_VALUES[(i + 2) % TEST_VALUES.length],
                TEST_VALUES[(i + 3) % TEST_VALUES.length]));
        }

        return data;
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSerializationRoundTrip(final String driverName,
        final String url,
        final String user,
        final String password) throws Exception {
        final IDataSourceConfiguration config = new SimpleDataSourceConfiguration(driverName, url, user, password);

        final DataSourcePropertySerializer serializer = new DataSourcePropertySerializer(config);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Writer writer = new OutputStreamWriter(out, Charsets.UTF_8)) {
            serializer.writeTo(writer);
        }

        final Properties props = new Properties();
        props.load(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(Strings.nullToEmpty(driverName),
            props.getProperty(DatabaseConstants.PROP_JDBC_DRIVER),
            "driverName");
        assertEquals(Strings.nullToEmpty(url), props.getProperty(DatabaseConstants.PROP_JDBC_URL), "url");
        assertEquals(Strings.nullToEmpty(user), props.getProperty(DatabaseConstants.PROP_JDBC_USER), "user");
        assertEquals(Strings.nullToEmpty(password),
            props.getProperty(DatabaseConstants.PROP_JDBC_PASSWORD),
            "password");
    }
}
